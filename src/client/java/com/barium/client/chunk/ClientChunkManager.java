package com.barium.client.chunk;

import com.barium.client.util.ChunkRenderManager;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.WorldChunk;

import java.util.Comparator;
import java.util.PriorityQueue;

/**
 * Client-side chunk rendering + processing optimizer.
 *
 * Hybrid model: cylinder in XZ + vertical section range in Y.
 */
public final class ClientChunkManager {
    private static final ClientChunkManager INSTANCE = new ClientChunkManager();

    // Defaults kept conservative for visual correctness.
    private static final int HORIZONTAL_RADIUS_CHUNKS = 12;
    private static final int VERTICAL_RANGE_SECTIONS = 8;
    private static final int MAX_MESH_BUILDS_PER_FRAME = 12;

    private final Long2ObjectOpenHashMap<ChunkRenderState> chunkStates = new Long2ObjectOpenHashMap<>();
    private final Long2ObjectOpenHashMap<SectionRenderState[]> sectionStates = new Long2ObjectOpenHashMap<>();
    private final PriorityQueue<ChunkRenderState> meshBuildQueue =
        new PriorityQueue<>(Comparator.comparingDouble(ChunkRenderState::priorityScore).reversed());

    private Frustum frustum;
    private int frameId;

    public static ClientChunkManager getInstance() {
        return INSTANCE;
    }

    private ClientChunkManager() {
    }

    public void setFrustum(Frustum frustum) {
        this.frustum = frustum;
    }

    public void update(Camera camera, ClientPlayerEntity player) {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientWorld world = client.world;
        if (world == null || player == null || camera == null) {
            return;
        }

        frameId++;
        meshBuildQueue.clear();

        ChunkPos center = player.getChunkPos();
        Vec3d look = player.getRotationVec(1.0F);
        int cameraSectionY = MathHelper.floor(camera.getPos().y) >> 4;
        int bottomSection = world.getBottomY() >> 4;

        for (int dx = -HORIZONTAL_RADIUS_CHUNKS; dx <= HORIZONTAL_RADIUS_CHUNKS; dx++) {
            for (int dz = -HORIZONTAL_RADIUS_CHUNKS; dz <= HORIZONTAL_RADIUS_CHUNKS; dz++) {
                double dist = Math.sqrt(dx * dx + dz * dz);
                if (dist > HORIZONTAL_RADIUS_CHUNKS) {
                    continue; // cylinder base (circle) in horizontal plane
                }

                int chunkX = center.x + dx;
                int chunkZ = center.z + dz;
                long key = ChunkPos.toLong(chunkX, chunkZ);
                ChunkRenderState renderState = chunkStates.get(key);
                if (renderState == null) {
                    renderState = new ChunkRenderState(new ChunkPos(chunkX, chunkZ));
                    chunkStates.put(key, renderState);
                }

                boolean chunkVisible = ChunkRenderManager.getInstance().isChunkInFrustum(chunkX, chunkZ);
                float score = computePriorityScore(dx, dz, dist, look, chunkVisible);
                renderState.setVisible(chunkVisible);
                renderState.setPriorityScore(score);

                updateVisibleChunks(world, chunkX, chunkZ, key, cameraSectionY, bottomSection, renderState);
            }
        }

        scheduleMeshBuilds();
    }

    public void updateVisibleChunks(ClientWorld world, int chunkX, int chunkZ, long chunkKey, int cameraSectionY, int bottomSection, ChunkRenderState chunkState) {
        WorldChunk chunk = world.getChunk(chunkX, chunkZ);
        if (chunk == null) {
            return;
        }

        ChunkSection[] sections = chunk.getSectionArray();
        SectionRenderState[] states = sectionStates.get(chunkKey);
        if (states == null || states.length != sections.length) {
            states = new SectionRenderState[sections.length];
            for (int i = 0; i < sections.length; i++) {
                states[i] = new SectionRenderState(i, sections[i] == null || sections[i].isEmpty());
            }
            sectionStates.put(chunkKey, states);
        }

        for (int i = 0; i < sections.length; i++) {
            ChunkSection section = sections[i];
            SectionRenderState sectionState = states[i];

            boolean isEmpty = section == null || section.isEmpty();
            sectionState.setEmpty(isEmpty);
            if (isEmpty) {
                continue; // NEVER render or build empty sections
            }

            int sectionY = bottomSection + i;
            int verticalDistance = Math.abs(cameraSectionY - sectionY);
            boolean inVerticalRange = verticalDistance <= VERTICAL_RANGE_SECTIONS;
            sectionState.setInVerticalRange(inVerticalRange);

            boolean sectionVisible = inVerticalRange
                && chunkState.isVisible()
                && ChunkRenderManager.getInstance().isSectionInFrustum(chunkX, sectionY, chunkZ);

            sectionState.setVisible(sectionVisible);
            sectionState.setNeedsMeshUpdate(sectionVisible || (inVerticalRange && chunkState.priorityScore() > 80.0f));

            if (sectionState.needsMeshUpdate()) {
                meshBuildQueue.add(chunkState);
            }
        }
    }

    public void scheduleMeshBuilds() {
        int budget = MAX_MESH_BUILDS_PER_FRAME;
        while (budget-- > 0 && !meshBuildQueue.isEmpty()) {
            ChunkRenderState state = meshBuildQueue.poll();
            // Integration hook:
            // Submit rebuild for the chunk only when visible/near-visible.
            // This keeps mesh work out of off-screen/far chunks and reduces stutter.
        }
    }

    public boolean shouldBuildChunkMesh(BlockPos origin) {
        int chunkX = origin.getX() >> 4;
        int chunkZ = origin.getZ() >> 4;

        ChunkRenderState state = chunkStates.get(ChunkPos.toLong(chunkX, chunkZ));
        if (state == null) {
            return true; // fallback to vanilla path until first visibility pass
        }

        return state.priorityScore() >= 40.0f;
    }

    private float computePriorityScore(int dx, int dz, double dist, Vec3d look, boolean isVisible) {
        if (dx == 0 && dz == 0) {
            return 10_000.0f;
        }

        Vec3d direction = new Vec3d(dx, 0.0, dz).normalize();
        double dot = look.dotProduct(direction);

        float visibilityBoost = isVisible ? 40.0f : 0.0f;
        float directionBoost = (float) (dot * 20.0);
        float distancePenalty = (float) (dist * 5.0);
        return 100.0f + visibilityBoost + directionBoost - distancePenalty;
    }

    public void clear() {
        chunkStates.clear();
        sectionStates.clear();
        meshBuildQueue.clear();
        frustum = null;
    }
}
