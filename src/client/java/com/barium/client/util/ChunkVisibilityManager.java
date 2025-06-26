// Em: src/client/java/com/barium/client/util/ChunkVisibilityManager.java
package com.barium.client.util;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

import java.util.concurrent.atomic.AtomicReference;

public class ChunkVisibilityManager {
    private static final ChunkVisibilityManager INSTANCE = new ChunkVisibilityManager();
    public static ChunkVisibilityManager getInstance() { return INSTANCE; }

    // Configurações da otimização
    private static final int RAYS_TO_CAST = 128; // Número de raios a disparar. Mais raios = mais preciso, mais pesado.
    private static final double MAX_RAY_DISTANCE = 128.0; // Distância máxima dos raios.
    private static final long UPDATE_INTERVAL_MS = 250; // Atualiza a visibilidade a cada 250ms
    private static final double MIN_MOVE_DISTANCE_SQ = 16.0; // Ou se o jogador se mover 4 blocos (4*4=16)

    private final AtomicReference<LongSet> visibleChunkKeys = new AtomicReference<>(new LongOpenHashSet());
    private long lastUpdateTime = 0;
    private Vec3d lastPlayerPos = Vec3d.ZERO;

    public void update(MinecraftClient client) {
        if (client.player == null || client.world == null || client.cameraEntity == null) return;

        long now = System.currentTimeMillis();
        Vec3d cameraPos = client.cameraEntity.getEyePos();

        // Evita recalcular constantemente se o jogador estiver parado
        boolean needsUpdate = (now - lastUpdateTime > UPDATE_INTERVAL_MS) || 
                              (cameraPos.squaredDistanceTo(lastPlayerPos) > MIN_MOVE_DISTANCE_SQ);

        if (!needsUpdate) {
            return;
        }

        this.lastUpdateTime = now;
        this.lastPlayerPos = cameraPos;

        LongSet newVisibleChunks = new LongOpenHashSet();
        
        // Sempre adiciona o chunk em que a câmera está
        newVisibleChunks.add(new ChunkPos(client.cameraEntity.getBlockPos()).toLong());

        // Usa uma Esfera de Fibonacci para distribuir os raios uniformemente
        double goldenRatio = (1.0 + Math.sqrt(5.0)) / 2.0;
        double angleIncrement = Math.PI * 2.0 * goldenRatio;

        for (int i = 0; i < RAYS_TO_CAST; i++) {
            // Matemática para gerar pontos em uma esfera
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

            // AQUI ESTÁ A CORREÇÃO CRÍTICA: Traçamos o caminho do raio
            traceRayAndAddChunks(cameraPos, hitResult.getPos(), newVisibleChunks);
        }
        
        visibleChunkKeys.set(newVisibleChunks);
    }

    /**
     * Usa uma variação do algoritmo de linha de Bresenham para "caminhar" ao longo de um raio 
     * e adicionar todos os chunks que ele atravessa ao conjunto de visíveis.
     */
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

    /**
     * Verifica se um chunk está na lista de chunks potencialmente visíveis.
     * Chamado pelo nosso Mixin.
     */
    public boolean isChunkPotentiallyVisible(int chunkX, int chunkZ) {
        LongSet visibleSet = visibleChunkKeys.get();
        // Se o cálculo ainda não rodou, considera tudo como visível para evitar problemas.
        if (visibleSet == null || visibleSet.isEmpty()) {
            return true;
        }
        return visibleSet.contains(ChunkPos.toLong(chunkX, chunkZ));
    }
}