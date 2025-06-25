package com.barium.client.util;

import com.barium.config.BariumConfig;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

import java.util.concurrent.atomic.AtomicReference;

public class ChunkVisibilityManager {
    private static final ChunkVisibilityManager INSTANCE = new ChunkVisibilityManager();
    public static ChunkVisibilityManager getInstance() { return INSTANCE; }

    private static final int RAYS_TO_CAST = 128;
    private static final double MAX_RAY_DISTANCE = 128.0; // Aumentar um pouco a distância do raio

    private final AtomicReference<LongSet> visibleChunkKeys = new AtomicReference<>(new LongOpenHashSet());
    private long lastUpdateTime = 0;
    private Vec3d lastPlayerPos = Vec3d.ZERO;

    public void update(MinecraftClient client) {
        if (client.player == null || client.world == null) return;

        long now = System.currentTimeMillis();
        Vec3d playerPos = client.player.getPos();

        if (now - lastUpdateTime < 500 && playerPos.squaredDistanceTo(lastPlayerPos) < 256) {
            return;
        }

        this.lastUpdateTime = now;
        this.lastPlayerPos = playerPos;

        LongSet newVisibleChunks = new LongOpenHashSet();
        Vec3d cameraPos = client.cameraEntity.getEyePos();
        
        newVisibleChunks.add(new ChunkPos(client.player.getBlockPos()).toLong());

        double goldenRatio = (1.0 + Math.sqrt(5.0)) / 2.0;
        double angleIncrement = Math.PI * 2.0 * goldenRatio;

        for (int i = 0; i < RAYS_TO_CAST; i++) {
            double t = (double) i / RAYS_TO_CAST;
            double inclination = Math.acos(1 - 2 * t);
            double azimuth = angleIncrement * i;

            double x = Math.sin(inclination) * Math.cos(azimuth);
            double y = Math.sin(inclination) * Math.sin(azimuth);
            double z = Math.cos(inclination);
            
            Vec3d direction = new Vec3d(x, y, z);
            Vec3d targetPos = cameraPos.add(direction.multiply(MAX_RAY_DISTANCE));

            RaycastContext context = new RaycastContext(cameraPos, targetPos, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, client.player);
            BlockHitResult hitResult = client.world.raycast(context);

            // --- A GRANDE CORREÇÃO ESTÁ AQUI ---
            traceRayAndAddChunks(cameraPos, hitResult.getPos(), newVisibleChunks);
        }
        
        visibleChunkKeys.set(newVisibleChunks);
    }

    /**
     * "Caminha" ao longo de um raio e adiciona todos os chunks que ele atravessa.
     * @param start Posição inicial do raio (câmera).
     * @param end Posição final do raio (onde ele atingiu um bloco ou a distância máxima).
     * @param chunkSet O conjunto para adicionar os chunks visíveis.
     */
    private void traceRayAndAddChunks(Vec3d start, Vec3d end, LongSet chunkSet) {
        int startChunkX = (int)start.getX() >> 4;
        int startChunkZ = (int)start.getZ() >> 4;
        int endChunkX = (int)end.getX() >> 4;
        int endChunkZ = (int)end.getZ() >> 4;

        chunkSet.add(ChunkPos.toLong(startChunkX, startChunkZ));
        
        int dx = Math.abs(endChunkX - startChunkX);
        int dz = Math.abs(endChunkZ - startChunkZ);
        int sx = startChunkX < endChunkX ? 1 : -1;
        int sz = startChunkZ < endChunkZ ? 1 : -1;
        int err = dx - dz;

        while(startChunkX != endChunkX || startChunkZ != endChunkZ) {
            chunkSet.add(ChunkPos.toLong(startChunkX, startChunkZ));
            int e2 = 2 * err;
            if (e2 > -dz) {
                err -= dz;
                startChunkX += sx;
            }
            if (e2 < dx) {
                err += dx;
                startChunkZ += sz;
            }
        }
        chunkSet.add(ChunkPos.toLong(endChunkX, endChunkZ));
    }

    public boolean isChunkPotentiallyVisible(int chunkX, int chunkZ) {
        LongSet visibleSet = visibleChunkKeys.get();
        if (visibleSet == null || visibleSet.isEmpty()) {
            return true;
        }
        return visibleSet.contains(ChunkPos.toLong(chunkX, chunkZ));
    }
}