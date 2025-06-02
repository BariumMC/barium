package com.barium.client.optimization;

import com.barium.BariumMod;
import com.barium.config.BariumConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.chunk.ChunkRender;
import net.minecraft.client.render.chunk.ChunkRenderDispatcher; // Corrected import for inner class reference
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;

/**
 * Otimiza a renderização de chunks (seções do mundo) aplicando culling baseado em CPU.
 * Este otimizador implementa heurísticas "ultra-baratas" para reduzir o número de chunks
 * enviados ao pipeline de renderização, especialmente benéfico para renderizadores de software como llvmpipe.
 *
 * Baseado nos mappings Yarn 1.21.5+build.1.
 */
public class ChunkOcclusionOptimizer { // This class should be ChunkOcclusionOptimizer, not WorldRendererMixin

    // Cache para a posição da câmera para determinar se a lógica de culling precisa ser reavaliada
    private static Vec3d lastCameraPos = Vec3d.ZERO;
    private static BlockPos lastCameraBlockPos = BlockPos.ORIGIN;
    private static long lastCullEvalTime = 0;
    private static final long CULL_EVAL_INTERVAL_MS = 250; // Reavalia lógica complexa a cada 250ms

    /**
     * Inicializa o otimizador de oclusão de chunks.
     */
    public static void init() {
        BariumMod.LOGGER.info("Inicializando ChunkOcclusionOptimizer");
    }

    /**
     * Determina se uma seção de chunk deve ser renderizada com base em heurísticas de oclusão do lado da CPU.
     * Este método visa ser "ultra-barato" e binário (renderiza/não renderiza).
     *
     * @param chunkRender O objeto ChunkRender que representa a seção do chunk.
     * @param camera A câmera atual do jogo.
     * @return true se a seção do chunk deve ser renderizada, false se deve ser descartada (culled).
     */
    public static boolean shouldRenderChunk(ChunkRender chunkRender, Camera camera) {
        if (!BariumConfig.ENABLE_CHUNK_OCCLUSION_CULLING) {
            return true; // Renderiza normalmente se a otimização estiver desativada
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.world == null || client.player == null) {
            return true; // Não pode realizar culling avançado sem o cliente, mundo ou jogador.
        }

        BlockView world = client.world;
        Box chunkBox = chunkRender.getBox();
        Vec3d cameraPos = camera.getPos();

        // Checa se a posição da câmera mudou significativamente ou se o intervalo passou para reavaliar a lógica complexa
        long currentTime = System.currentTimeMillis();
        boolean reEvaluateComplexCulling = false;
        if ((currentTime - lastCullEvalTime) > CULL_EVAL_INTERVAL_MS || !cameraPos.equals(lastCameraPos)) {
            reEvaluateComplexCulling = true;
            lastCullEvalTime = currentTime;
            lastCameraPos = cameraPos;
            lastCameraBlockPos = BlockPos.ofFloored(cameraPos);
        }

        // Heurística 1: Culling agressivo se o jogador estiver dentro de um bloco opaco.
        // Isso é útil para quando o jogador está "enterrado" em uma parede ou montanha,
        // evitando renderizar o mundo exterior desnecessariamente.
        if (reEvaluateComplexCulling) {
            if (world.getBlockState(lastCameraBlockPos).isOpaqueFullCube(world, lastCameraBlockPos)) {
                double localDistanceSq = cameraPos.squaredDistanceTo(chunkBox.getCenter());
                if (localDistanceSq > BariumConfig.PLAYER_IN_BLOCK_CULL_DISTANCE_SQ) {
                    // BariumMod.LOGGER.debug("Culling chunk {} (player in block, dist {})", chunkRender.getOrigin(), Math.sqrt(localDistanceSq));
                    return false; // Culla o chunk se estiver além da distância de culling quando o jogador está em um bloco
                }
            }
        }

        // Heurística 2: Culling direcional agressivo para chunks distantes que estão estritamente "atrás" da visão da câmera.
        // Isso complementa o culling de frustum e é útil para `llvmpipe` ao reduzir draw calls para objetos
        // claramente fora do campo de visão frontal.
        double distanceSq = cameraPos.squaredDistanceTo(chunkBox.getCenter());
        if (distanceSq > BariumConfig.AGGRESSIVE_DIRECTIONAL_CULL_DISTANCE_SQ) {
            Vec3d chunkCenter = chunkBox.getCenter();
            Vec3d cameraToChunk = chunkCenter.subtract(cameraPos).normalize();
            Vec3d cameraLook = camera.getRotation().normalize(); // Vetor de direção frontal da câmera

            // Calcula o produto escalar: negativo significa que o chunk está "atrás" do olhar da câmera.
            // Um valor como -0.5 indica que está aproximadamente 120 graus ou mais fora do eixo frontal.
            double dot = cameraLook.dot(cameraToChunk);
            if (dot < -0.5) { // Limiar ajustável para culling mais/menos agressivo
                // BariumMod.LOGGER.debug("Culling chunk {} (directional, dot {}, dist {})", chunkRender.getOrigin(), dot, Math.sqrt(distanceSq));
                return false;
            }
        }

        return true; // Renderiza o chunk se nenhuma heurística de culling se aplicar
    }
}