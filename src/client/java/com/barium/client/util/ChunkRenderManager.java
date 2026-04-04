package com.barium.client.util;

import com.barium.config.BariumConfig;
import it.unimi.dsi.fastutil.longs.Long2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Frustum;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;

public class ChunkRenderManager {
    private static final ChunkRenderManager INSTANCE = new ChunkRenderManager();
    public static ChunkRenderManager getInstance() { return INSTANCE; }

    private Frustum frustum;
    private Vec3d lastCameraPos;
    private float lastYaw = Float.NaN;
    private float lastPitch = Float.NaN;
    private static final double CACHE_REUSE_POS_DELTA_SQ = 0.05 * 0.05;
    private static final float CACHE_REUSE_ROT_DELTA = 0.75f;

    private final Long2BooleanOpenHashMap chunkVisibilityCache = new Long2BooleanOpenHashMap();
    private final Long2BooleanOpenHashMap sectionVisibilityCache = new Long2BooleanOpenHashMap();
    private final LongOpenHashSet predictedVisibleSections = new LongOpenHashSet();

    /**
    Atualiza o frustum atual. Chamado a cada frame pelo WorldRendererMixin.
    */
    public void setFrustum(Frustum frustum) {
        this.frustum = frustum;
    }

    public void beginFrame(Vec3d cameraPos) {
        if (BariumConfig.C.ENABLE_FRAME_VISIBILITY_CACHE && shouldInvalidateVisibilityCache(cameraPos)) {
            chunkVisibilityCache.clear();
            sectionVisibilityCache.clear();
        }
        rebuildPredictedVisibility(cameraPos);
    }

    /**
    Verifica se um chunk está dentro do Frustum (campo de visão) da câmera.
    */
    public boolean isChunkInFrustum(int chunkX, int chunkZ) {
        long cacheKey = ChunkPos.toLong(chunkX, chunkZ);
        if (BariumConfig.C.ENABLE_FRAME_VISIBILITY_CACHE && chunkVisibilityCache.containsKey(cacheKey)) {
            return chunkVisibilityCache.get(cacheKey);
        }

        boolean visible = computeChunkInFrustum(chunkX, chunkZ);
        if (BariumConfig.C.ENABLE_FRAME_VISIBILITY_CACHE) {
            chunkVisibilityCache.put(cacheKey, visible);
        }

        return visible;
    }

    public boolean isSectionInFrustum(int sectionX, int sectionY, int sectionZ) {
        long cacheKey = BlockPos.asLong(sectionX, sectionY, sectionZ);
        if (BariumConfig.C.ENABLE_FRAME_VISIBILITY_CACHE && sectionVisibilityCache.containsKey(cacheKey)) {
            return sectionVisibilityCache.get(cacheKey);
        }

        boolean visible;
        if (this.frustum == null) {
            visible = true;
        } else {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.world == null || client.player == null) {
                visible = true;
            } else {
                double minX = sectionX * 16.0;
                double minY = sectionY * 16.0;
                double minZ = sectionZ * 16.0;
                double maxX = minX + 16.0;
                double maxY = minY + 16.0;
                double maxZ = minZ + 16.0;
                visible = frustum.isVisible(new Box(minX, minY, minZ, maxX, maxY, maxZ));
            }
        }

        if (BariumConfig.C.ENABLE_FRAME_VISIBILITY_CACHE) {
            sectionVisibilityCache.put(cacheKey, visible);
        }

        return visible;
    }

    public boolean isSectionPredictedVisible(int sectionX, int sectionY, int sectionZ) {
        if (!BariumConfig.C.ENABLE_PREDICTIVE_OCCLUSION_CULLING) {
            return false;
        }
        return predictedVisibleSections.contains(BlockPos.asLong(sectionX, sectionY, sectionZ));
    }

    private void rebuildPredictedVisibility(Vec3d cameraPos) {
        predictedVisibleSections.clear();

        if (!BariumConfig.C.ENABLE_PREDICTIVE_OCCLUSION_CULLING || cameraPos == null) {
            lastCameraPos = cameraPos;
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null || client.player == null || this.frustum == null) {
            lastCameraPos = cameraPos;
            return;
        }

        if (lastCameraPos == null) {
            lastCameraPos = cameraPos;
            return;
        }

        Vec3d frameMotion = cameraPos.subtract(lastCameraPos);
        double motionLength = frameMotion.length();
        if (motionLength < 1.0e-3) {
            lastCameraPos = cameraPos;
            return;
        }

        double lookAheadFrames = Math.max(1.0, BariumConfig.C.PREDICTIVE_LOOKAHEAD_MS / 50.0);
        Vec3d predictedTravel = frameMotion.multiply(lookAheadFrames);
        Vec3d motionDir = predictedTravel.normalize();

        int cameraSectionX = (int) Math.floor(cameraPos.x / 16.0);
        int cameraSectionY = (int) Math.floor(cameraPos.y / 16.0);
        int cameraSectionZ = (int) Math.floor(cameraPos.z / 16.0);

        int extraForwardSections = Math.max(1, (int) Math.ceil(predictedTravel.length() / 16.0)) + BariumConfig.C.PREDICTIVE_FORWARD_EXTRA_SECTIONS;
        int horizontalRadius = Math.max(2, BariumConfig.C.PREDICTIVE_HORIZONTAL_RADIUS_SECTIONS);
        int verticalRadius = Math.max(1, BariumConfig.C.PREDICTIVE_VERTICAL_RADIUS_SECTIONS);

        for (int x = cameraSectionX - horizontalRadius; x <= cameraSectionX + horizontalRadius; x++) {
            for (int y = cameraSectionY - verticalRadius; y <= cameraSectionY + verticalRadius; y++) {
                for (int z = cameraSectionZ - horizontalRadius; z <= cameraSectionZ + horizontalRadius; z++) {
                    double toX = (x - cameraSectionX) + 0.5;
                    double toY = (y - cameraSectionY) + 0.5;
                    double toZ = (z - cameraSectionZ) + 0.5;

                    double forward = toX * motionDir.x + toY * motionDir.y + toZ * motionDir.z;
                    if (forward < -1.0 || forward > extraForwardSections) {
                        continue;
                    }

                    double lenSq = toX * toX + toY * toY + toZ * toZ;
                    double sideSq = Math.max(0.0, lenSq - (forward * forward));
                    if (sideSq > (horizontalRadius * horizontalRadius)) {
                        continue;
                    }

                    if (isSectionInFrustum(x, y, z)) {
                        predictedVisibleSections.add(BlockPos.asLong(x, y, z));
                    }
                }
            }
        }

        lastCameraPos = cameraPos;
    }

    private boolean shouldInvalidateVisibilityCache(Vec3d cameraPos) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null || cameraPos == null) {
            return true;
        }

        if (lastCameraPos == null || Float.isNaN(lastYaw) || Float.isNaN(lastPitch)) {
            lastYaw = client.player.getYaw();
            lastPitch = client.player.getPitch();
            return true;
        }

        float yaw = client.player.getYaw();
        float pitch = client.player.getPitch();
        float yawDelta = Math.abs(yaw - lastYaw);
        float pitchDelta = Math.abs(pitch - lastPitch);
        double movementSq = cameraPos.squaredDistanceTo(lastCameraPos);

        boolean invalidate = movementSq > CACHE_REUSE_POS_DELTA_SQ
                || yawDelta > CACHE_REUSE_ROT_DELTA
                || pitchDelta > CACHE_REUSE_ROT_DELTA;

        lastYaw = yaw;
        lastPitch = pitch;
        return invalidate;
    }

    private boolean computeChunkInFrustum(int chunkX, int chunkZ) {
        if (this.frustum == null) return true;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null || client.player == null) return true;

        double minX = chunkX * 16.0;
        double minZ = chunkZ * 16.0;
        double maxX = minX + 16.0;
        double maxZ = minZ + 16.0;

        final double margin = 1.0;
        minX -= margin; minZ -= margin;
        maxX += margin; maxZ += margin;

        double minY = client.world.getBottomY();
        double maxY = client.world.getHeight();

        boolean visible = frustum.isVisible(new Box(minX, minY, minZ, maxX, maxY, maxZ));
        if (visible) return true;

        int renderDistance = client.options.getViewDistance().getValue();
        int effective = BariumConfig.C.EFFECTIVE_RENDER_DISTANCE > 0 ? BariumConfig.C.EFFECTIVE_RENDER_DISTANCE : renderDistance;
        int playerChunkX = client.player.getChunkPos().x;
        int playerChunkZ = client.player.getChunkPos().z;
        int dx = Math.abs(chunkX - playerChunkX);
        int dz = Math.abs(chunkZ - playerChunkZ);

        if (dx <= effective && dz <= effective) {
            if (dx <= 2 && dz <= 2) return true;

            int sparse = Math.max(1, BariumConfig.C.SPARSE_CHUNK_FACTOR);
            if (sparse <= 1) return true;

            return Math.floorMod(chunkX - playerChunkX, sparse) == 0 && Math.floorMod(chunkZ - playerChunkZ, sparse) == 0;
        }

        return false;
    }

    public void calculateChunksToRender(MinecraftClient client, Frustum frustum) {
        this.setFrustum(frustum);
    }

    public void clear() {
        this.frustum = null;
        this.chunkVisibilityCache.clear();
        this.sectionVisibilityCache.clear();
        this.predictedVisibleSections.clear();
        this.lastCameraPos = null;
    }
}
