package com.barium.client.util;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
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

        boolean needsUpdate = (now - lastUpdateTime > UPDATE_INTERVAL_MS) ||
                              (cameraPos.squaredDistanceTo(lastCameraPos) > MIN_MOVE_DISTANCE_SQ);

        if (!needsUpdate) {
            return;
        }

        this.lastUpdateTime = now;
        this.lastCameraPos = cameraPos;

        LongSet newVisibleChunks = new LongOpenHashSet();

        // Regra de segurança para o chão próximo ao jogador (sempre visível)
        final int forceVisibleRadius = 2;
        ChunkPos playerChunkPos = new ChunkPos(client.cameraEntity.getBlockPos());
        for (int x = -forceVisibleRadius; x <= forceVisibleRadius; x++) {
            for (int z = -forceVisibleRadius; z <= forceVisibleRadius; z++) {
                newVisibleChunks.add(ChunkPos.toLong(playerChunkPos.x + x, playerChunkPos.z + z));
            }
        }

        // Lógica de Ray-casting
        boolean isSpectator = client.player.isSpectator();
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
            Vec3d endPos;

            if (isSpectator) {
                // ==================================================================
                // INÍCIO DA LÓGICA AVANÇADA PARA ESPECTADOR
                // ==================================================================
                // ESTÁGIO 1: Raio de Escape - viaja através de blocos sólidos
                RaycastContext escapeContext = new RaycastContext(
                    cameraPos,
                    cameraPos.add(direction.multiply(MAX_RAY_DISTANCE)),
                    RaycastContext.ShapeType.COLLIDER,
                    RaycastContext.FluidHandling.NONE,
                    client.player
                ) {
                    // Aqui está a mágica: Nós mentimos para o ray-caster.
                    @Override
                    public BlockState getBlockState(BlockPos pos) {
                        BlockState realState = client.world.getBlockState(pos);
                        // Se for um bloco sólido, finja que é ar para o raio passar.
                        return realState.isOpaqueFullCube(client.world, pos) ? Blocks.AIR.getDefaultState() : realState;
                    }
                };
                BlockHitResult escapeHit = client.world.raycast(escapeContext);

                // Se o raio de escape não encontrou ar, não há nada a renderizar nessa direção.
                if (escapeHit.getType() == HitResult.Type.MISS) {
                    continue;
                }
                
                // ESTÁGIO 2: Raio de Visibilidade - começa onde encontramos ar e se comporta normalmente.
                Vec3d visibilityStartPos = escapeHit.getPos();
                RaycastContext visibilityContext = new RaycastContext(
                    visibilityStartPos,
                    visibilityStartPos.add(direction.multiply(MAX_RAY_DISTANCE)),
                    RaycastContext.ShapeType.COLLIDER,
                    RaycastContext.FluidHandling.NONE,
                    client.player
                );
                BlockHitResult visibilityHit = client.world.raycast(visibilityContext);
                endPos = visibilityHit.getPos();
                // ==================================================================
                // FIM DA LÓGICA AVANÇADA PARA ESPECTADOR
                // ==================================================================
            } else {
                // Comportamento normal para outros modos de jogo
                RaycastContext context = new RaycastContext(
                    cameraPos,
                    cameraPos.add(direction.multiply(MAX_RAY_DISTANCE)),
                    RaycastContext.ShapeType.COLLIDER,
                    RaycastContext.FluidHandling.NONE,
                    client.player
                );
                BlockHitResult hitResult = client.world.raycast(context);
                endPos = hitResult.getPos();
            }

            traceRayAndAddChunks(cameraPos, endPos, newVisibleChunks);
        }

        visibleChunkKeys.set(newVisibleChunks);
    }

    private void traceRayAndAddChunks(Vec3d start, Vec3d end, LongSet chunkSet) {
        int x1 = (int)start.getX() >> 4;
        int z1 = (int)start.getZ() >> 4;
        int x2 = (int)end.getX() >> 4;
        int z2 = (int)end.getZ() >> 4;

        int dx = Math.abs(x2 - x1);
        int dz = Math.abs(z2 - z1);
        int sx = x1 < x2 ? 1 : -1;
        int sz = z1 < z2 ? 1 : -1;
        int err = dx - dz;

        while(true) {
            chunkSet.add(ChunkPos.toLong(x1, z1));
            if (x1 == x2 && z1 == z2) break;

            int e2 = 2 * err;
            if (e2 > -dz) {
                err -= dz;
                x1 += sx;
            }
            if (e2 < dx) {
                err += dx;
                z1 += sz;
            }
        }
    }

    public boolean isChunkPotentiallyVisible(int chunkX, int chunkZ) {
        LongSet visibleSet = visibleChunkKeys.get();
        if (visibleSet == null || visibleSet.isEmpty()) {
            return true;
        }
        return visibleSet.contains(ChunkPos.toLong(chunkX, chunkZ));
    }
}