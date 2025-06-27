package com.barium.client.util;

import com.barium.client.BariumClient;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;

public class ChunkVisibilityManager {
    private static final ChunkVisibilityManager INSTANCE = new ChunkVisibilityManager();
    public static ChunkVisibilityManager getInstance() { return INSTANCE; }

    private static final int RAYS_TO_CAST = 100;
    private static final double MAX_RAY_DISTANCE = 160.0;
    private static final long UPDATE_INTERVAL_MS = 250;

    private final AtomicReference<LongSet> visibleChunkKeys = new AtomicReference<>(null);
    private Future<?> visibilityTask = null;

    public void update(MinecraftClient client) {
        if (client.player == null || client.world == null) return;
        if (visibilityTask != null && !visibilityTask.isDone()) return;

        visibilityTask = BariumClient.RENDER_THREAD_POOL.submit(() -> rebuildVisibilityMap(client));
    }

    // Este método roda na thread do Barium, não na do jogo.
    private void rebuildVisibilityMap(MinecraftClient client) {
        if (client.player == null || client.world == null || client.cameraEntity == null) return;

        Vec3d cameraPos = client.cameraEntity.getEyePos();
        LongSet initialHits = new LongOpenHashSet(); // Apenas os chunks diretamente atingidos

        // Lógica de Ray-casting para encontrar os chunks visíveis
        boolean isSpectator = client.player.isSpectator();
        for (int i = 0; i < RAYS_TO_CAST; i++) {
            double goldenRatio = (1.0 + Math.sqrt(5.0)) / 2.0;
            double angleIncrement = Math.PI * 2.0 * goldenRatio;
            double t = (double) i / RAYS_TO_CAST;
            double inclination = Math.acos(1 - 2 * t);
            double azimuth = angleIncrement * i;
            Vec3d direction = new Vec3d(Math.sin(inclination) * Math.cos(azimuth), Math.sin(inclination) * Math.sin(azimuth), Math.cos(inclination));

            Vec3d finalHitPos;
            if (isSpectator) {
                Vec3d escapePoint = findFirstNonOpaqueBlock(client, cameraPos, direction);
                if (escapePoint == null) continue;
                RaycastContext context = new RaycastContext(escapePoint, escapePoint.add(direction.multiply(MAX_RAY_DISTANCE)), RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, client.player);
                finalHitPos = client.world.raycast(context).getPos();
            } else {
                RaycastContext context = new RaycastContext(cameraPos, cameraPos.add(direction.multiply(MAX_RAY_DISTANCE)), RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, client.player);
                finalHitPos = client.world.raycast(context).getPos();
            }
            traceRayAndAddChunks(cameraPos, finalHitPos, initialHits);
        }

        // CORREÇÃO VISUAL: PASSE DE EXPANSÃO E SEGURANÇA
        LongSet finalVisibleChunks = new LongOpenHashSet();
        final int forceVisibleRadius = 3; // Um "tapete" de segurança maior
        ChunkPos playerChunkPos = client.player.getChunkPos();

        // 1. Adiciona o tapete de segurança
        for (int x = -forceVisibleRadius; x <= forceVisibleRadius; x++) {
            for (int z = -forceVisibleRadius; z <= forceVisibleRadius; z++) {
                finalVisibleChunks.add(ChunkPos.toLong(playerChunkPos.x + x, playerChunkPos.z + z));
            }
        }
        
        // 2. Adiciona os chunks atingidos E seus vizinhos (expansão que corrige os buracos)
        for (long key : initialHits) {
            int x = ChunkPos.getPackedX(key);
            int z = ChunkPos.getPackedZ(key);
            finalVisibleChunks.add(key); // O próprio chunk
            finalVisibleChunks.add(ChunkPos.toLong(x + 1, z));
            finalVisibleChunks.add(ChunkPos.toLong(x - 1, z));
            finalVisibleChunks.add(ChunkPos.toLong(x, z + 1));
            finalVisibleChunks.add(ChunkPos.toLong(x, z - 1));
        }

        this.visibleChunkKeys.set(finalVisibleChunks);
    }

    private Vec3d findFirstNonOpaqueBlock(MinecraftClient client, Vec3d start, Vec3d direction) {
        final double step = 0.5;
        for (double d = 0; d < MAX_RAY_DISTANCE; d += step) {
            BlockPos blockPos = BlockPos.ofFloored(start.add(direction.multiply(d)));
            if (!client.world.isChunkLoaded(blockPos)) return null;
            BlockState state = client.world.getBlockState(blockPos);
            if (!state.isOpaque()) return start.add(direction.multiply(d));
        }
        return null;
    }

    private void traceRayAndAddChunks(Vec3d start, Vec3d end, LongSet chunkSet) {
        int x1 = (int) start.getX() >> 4, z1 = (int) start.getZ() >> 4;
        int x2 = (int) end.getX() >> 4, z2 = (int) end.getZ() >> 4;
        int dx = Math.abs(x2 - x1), dz = Math.abs(z2 - z1);
        int sx = x1 < x2 ? 1 : -1, sz = z1 < z2 ? 1 : -1;
        int err = dx - dz;
        while (true) {
            chunkSet.add(ChunkPos.toLong(x1, z1));
            if (x1 == x2 && z1 == z2) break;
            int e2 = 2 * err;
            if (e2 > -dz) { err -= dz; x1 += sx; }
            if (e2 < dx) { err += dx; z1 += sz; }
        }
    }

    public boolean isChunkPotentiallyVisible(int chunkX, int chunkZ) {
        LongSet visibleSet = visibleChunkKeys.get();
        if (visibleSet == null) return true; // Se o cálculo nunca rodou, não esconde nada.
        return visibleSet.contains(ChunkPos.toLong(chunkX, chunkZ));
    }
}