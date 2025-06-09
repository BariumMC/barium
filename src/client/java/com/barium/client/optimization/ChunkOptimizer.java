package com.barium.client.optimization;

import com.barium.BariumMod;
import com.barium.config.BariumConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.Vec3d;

/**
 * Otimiza a renderização de chunks e entidades de bloco.
 * Foca em culling de entidades de bloco para melhorar a performance.
 */
public class ChunkOptimizer {

    /**
     * Inicializa o ChunkOptimizer.
     */
    public static void init() {
        BariumMod.LOGGER.info("Inicializando ChunkOptimizer");
    }

    /**
     * Determina se uma entidade de bloco deve ser renderizada com base na distância.
     * @param blockEntity A entidade de bloco a ser verificada.
     * @param camera A câmera atual do jogador.
     * @return true se a entidade de bloco deve ser renderizada, false caso contrário.
     */
    public static boolean shouldRenderBlockEntity(BlockEntity blockEntity, Camera camera) {
        if (!BariumConfig.ENABLE_CHUNK_OPTIMIZATION || !BariumConfig.ENABLE_BLOCK_ENTITY_CULLING) {
            return true; // Se as otimizações estiverem desativadas, sempre renderiza
        }

        Vec3d blockEntityPos = Vec3d.ofCenter(blockEntity.getPos());
        Vec3d cameraPos = camera.getPos();

        double distanceSq = blockEntityPos.squaredDistanceTo(cameraPos);

        // Culling por distância
        return distanceSq <= BariumConfig.MAX_BLOCK_ENTITY_RENDER_DISTANCE_SQ;
    }
}