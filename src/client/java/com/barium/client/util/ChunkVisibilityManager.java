package com.barium.client.util;

import com.barium.client.BariumClient;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;

public class ChunkVisibilityManager {
    private static final ChunkVisibilityManager INSTANCE = new ChunkVisibilityManager();
    public static ChunkVisibilityManager getInstance() { return INSTANCE; }

    private static final int RAYS_TO_CAST = 128;
    private static final double MAX_RAY_DISTANCE = 256.0;
    private static final long UPDATE_INTERVAL_MS = 200;

    private final AtomicReference<LongSet> visibleChunkKeys = new AtomicReference<>(new LongOpenHashSet());
    private final AtomicReference<LongSet> visibleSectionKeys = new AtomicReference<>(new LongOpenHashSet());
    
    private Future<?> visibilityTask = null;
    private long lastUpdateTime = 0;

    public void update(MinecraftClient client) {
        if (client.player == null || client.world == null) return;
        if (visibilityTask != null && !visibilityTask.isDone()) return;
        
        long currentTime = System.currentTimeMillis();

        // CORREÇÃO: Removida a lógica complexa de verificação de movimento.
        // Agora, o sistema usa um temporizador simples. Isso garante que, se o jogador
        // girar a câmera (mesmo parado), a visibilidade será recalculada,
        // corrigindo o bug dos chunks invisíveis.
        if ((currentTime - lastUpdateTime) < UPDATE_INTERVAL_MS) {
            return;
        }

        lastUpdateTime = currentTime;
        visibilityTask = BariumClient.RENDER_THREAD_POOL.submit(() -> rebuildVisibilityMap(client));
    }

    private void rebuildVisibilityMap(MinecraftClient client) {
        if (client.player == null || client.world == null || client.cameraEntity == null) return;

        final Vec3d cameraPos = client.cameraEntity.getEyePos();
        final LongSet directlyHitChunks = new LongOpenHashSet();
        final LongSet hitSections = new LongOpenHashSet();

        for (int i = 0; i < RAYS_TO_CAST; i++) {
            Vec3d direction = getFibonacciSphereVector(i, RAYS_TO_CAST);
            Vec3d targetPos = cameraPos.add(direction.multiply(MAX_RAY_DISTANCE));
            
            RaycastContext context = new RaycastContext(cameraPos, targetPos, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, client.cameraEntity);
            BlockHitResult hitResult = client.world.raycast(context);

            if (hitResult.getType() != HitResult.Type.MISS) {
                BlockPos hitBlockPos = hitResult.getBlockPos();
                directlyHitChunks.add(ChunkPos.toLong(hitBlockPos));
                traceRayAndAddSections(cameraPos, hitResult.getPos(), hitSections);
            }
        }

        final LongSet finalVisibleChunks = new LongOpenHashSet();
        final int safetyRadius = 2;
        final ChunkPos playerChunkPos = client.player.getChunkPos();

        for (int x = -safetyRadius; x <= safetyRadius; x++) {
            for (int z = -safetyRadius; z <= safetyRadius; z++) {
                finalVisibleChunks.add(ChunkPos.toLong(playerChunkPos.x + x, playerChunkPos.z + z));
            }
        }
        for (long key : directlyHitChunks) {
            int x = ChunkPos.getPackedX(key);
            int z = ChunkPos.getPackedZ(key);
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    finalVisibleChunks.add(ChunkPos.toLong(x + dx, z + dz));
                }
            }
        }
        this.visibleChunkKeys.set(finalVisibleChunks);

        final LongSet finalVisibleSections = new LongOpenHashSet(hitSections);
        int playerSectionY = client.world.getSectionIndex(client.player.getBlockY());
        BlockPos playerSectionPos = new BlockPos(playerChunkPos.x, playerSectionY, playerChunkPos.z);
        for(int x = -1; x <= 1; x++) {
            for(int y = -1; y <= 1; y++) {
                for(int z = -1; z <= 1; z++) {
                    finalVisibleSections.add(BlockPos.asLong(playerSectionPos.getX() + x, playerSectionPos.getY() + y, playerSectionPos.getZ() + z));
                }
            }
        }
        this.visibleSectionKeys.set(finalVisibleSections);
    }

    private void traceRayAndAddSections(Vec3d start, Vec3d end, LongSet sectionSet) {
        int x1 = (int) Math.floor(start.getX() / 16);
        int y1 = (int) Math.floor(start.getY() / 16);
        int z1 = (int) Math.floor(start.getZ() / 16);

        int x2 = (int) Math.floor(end.getX() / 16);
        int y2 = (int) Math.floor(end.getY() / 16);
        int z2 = (int) Math.floor(end.getZ() / 16);

        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);
        int dz = Math.abs(z2 - z1);

        int sx = x1 < x2 ? 1 : -1;
        int sy = y1 < y2 ? 1 : -1;
        int sz = z1 < z2 ? 1 : -1;

        int err1 = dx - dy;
        int err2 = dx - dz;
        
        sectionSet.add(BlockPos.asLong(x1, y1, z1));

        while (x1 != x2 || y1 != y2 || z1 != z2) {
            int e1 = 2 * err1;
            int e2 = 2 * err2;
            
            boolean xMoved = false, yMoved = false, zMoved = false;

            if (e1 > -dy) { err1 -= dy; x1 += sx; xMoved = true; }
            if (e1 < dx) { err1 += dx; y1 += sy; yMoved = true; }
            if (e2 > -dz) { err2 -= dz; if (!xMoved) x1 += sx; }
            if (e2 < dx) { err2 += dx; z1 += sz; }
            
            sectionSet.add(BlockPos.asLong(x1, y1, z1));
        }
    }

    private Vec3d getFibonacciSphereVector(int i, int n) {
        double phi = Math.PI * (3.0 - Math.sqrt(5.0));
        double y = 1 - (i / (double)(n - 1)) * 2;
        double radius = Math.sqrt(1 - y * y);
        double theta = phi * i;
        double x = Math.cos(theta) * radius;
        double z = Math.sin(theta) * radius;
        return new Vec3d(x, y, z);
    }

    public boolean isChunkPotentiallyVisible(int chunkX, int chunkZ) {
        LongSet visibleSet = visibleChunkKeys.get();
        if (visibleSet == null) return true;
        return visibleSet.contains(ChunkPos.toLong(chunkX, chunkZ));
    }
    
    public boolean isSectionPotentiallyVisible(int sectionX, int sectionY, int sectionZ) {
        LongSet visibleSet = visibleSectionKeys.get();
        if (visibleSet == null) return true;
        return visibleSet.contains(BlockPos.asLong(sectionX, sectionY, sectionZ));
    }
    
    public void clear() {
        this.visibleChunkKeys.set(new LongOpenHashSet());
        this.visibleSectionKeys.set(new LongOpenHashSet());
        this.lastUpdateTime = 0;
    }
}