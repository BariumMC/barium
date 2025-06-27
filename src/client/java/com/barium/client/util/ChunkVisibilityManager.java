package com.barium.client.util;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

import java.util.concurrent.atomic.AtomicReference;

public class ChunkVisibilityManager {
    private static final ChunkVisibilityManager INSTANCE = new ChunkVisibilityManager();
    public static ChunkVisibilityManager getInstance() { return INSTANCE; }

    // Configurações da otimização
    private static final int RAYS_TO_CAST = 128;
    private static final double MAX_RAY_DISTANCE = 160.0;
    private static final long UPDATE_INTERVAL_MS = 250;
    private static final double MIN_MOVE_DISTANCE_SQ = 16.0;

    private final AtomicReference<LongSet> visibleChunkKeys = new AtomicReference<>(new LongOpenHashSet());
    private long lastUpdateTime = 0;
    private Vec3d lastCameraPos = Vec3d.ZERO;

    public void update(MinecraftClient client) {
        if (client.player == null || client.world == null || client.cameraEntity == null) return;

        long now = System.currentTimeMillis();
        Vec3d cameraPos = client.cameraEntity.getEyePos();

        boolean needsUpdate = (now - lastUpdateTime > UPDATE_INTERVAL_MS) || (cameraPos.squaredDistanceTo(lastCameraPos) > MIN_MOVE_DISTANCE_SQ);
        if (!needsUpdate) return;

        this.lastUpdateTime = now;
        this.lastCameraPos = cameraPos;

        LongSet initialChunks = new LongOpenHashSet();

        // ESTÁGIO 1: O TAPETE DE SEGURANÇA (Corrige chão desaparecendo)
        final int forceVisibleRadius = 2;
        ChunkPos playerChunkPos = client.player.getChunkPos();
        for (int x = -forceVisibleRadius; x <= forceVisibleRadius; x++) {
            for (int z = -forceVisibleRadius; z <= forceVisibleRadius; z++) {
                initialChunks.add(ChunkPos.toLong(playerChunkPos.x + x, playerChunkPos.z + z));
            }
        }

        // ESTÁGIO 2: RAY-CASTING INTELIGENTE (Encontra chunks distantes e lida com espectador)
        boolean isSpectator = client.player.isSpectator();
        double goldenRatio = (1.0 + Math.sqrt(5.0)) / 2.0;
        double angleIncrement = Math.PI * 2.0 * goldenRatio;

        for (int i = 0; i < RAYS_TO_CAST; i++) {
            double t = (double) i / RAYS_TO_CAST;
            double inclination = Math.acos(1 - 2 * t);
            double azimuth = angleIncrement * i;
            Vec3d direction = new Vec3d(Math.sin(inclination) * Math.cos(azimuth), Math.sin(inclination) * Math.sin(azimuth), Math.cos(inclination));

            Vec3d finalHitPos;
            if (isSpectator) {
                Vec3d escapePoint = findFirstNonOpaqueBlock(client, cameraPos, direction);
                if (escapePoint == null) continue;
                RaycastContext visibilityContext = new RaycastContext(escapePoint, escapePoint.add(direction.multiply(MAX_RAY_DISTANCE)), RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, client.player);
                finalHitPos = client.world.raycast(visibilityContext).getPos();
            } else {
                RaycastContext context = new RaycastContext(cameraPos, cameraPos.add(direction.multiply(MAX_RAY_DISTANCE)), RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, client.player);
                finalHitPos = client.world.raycast(context).getPos();
            }
            traceRayAndAddChunks(cameraPos, finalHitPos, initialChunks);
        }

        // ESTÁGIO 3: PREENCHIMENTO DE LACUNAS (Corrige as "torres" e buracos)
        LongSet finalVisibleChunks = new LongOpenHashSet(initialChunks);
        for (long key : initialChunks) {
            int x = ChunkPos.getPackedX(key);
            int z = ChunkPos.getPackedZ(key);
            finalVisibleChunks.add(ChunkPos.toLong(x + 1, z));
            finalVisibleChunks.add(ChunkPos.toLong(x - 1, z));
            finalVisibleChunks.add(ChunkPos.toLong(x, z + 1));
            finalVisibleChunks.add(ChunkPos.toLong(x, z - 1));
        }

        visibleChunkKeys.set(finalVisibleChunks);
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
        if (visibleSet == null || visibleSet.isEmpty()) return true;
        return visibleSet.contains(ChunkPos.toLong(chunkX, chunkZ));
    }
}