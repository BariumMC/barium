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

            /**
     * Verifica se uma entidade de bloco está ocluída (escondida) por geometria sólida.
     * @return true se a entidade de bloco estiver escondida.
     */
    public static boolean isBlockEntityOccluded(BlockEntity blockEntity, Camera camera) {
        if (!BariumConfig.ENABLE_BLOCK_ENTITY_OCCLUSION_CULLING) {
            return false; // Se a otimização estiver desligada, nunca está ocluído.
        }

        var world = blockEntity.getWorld();
        if (world == null) return false;

        Vec3d cameraPos = camera.getPos();
        Vec3d blockEntityPos = Vec3d.ofCenter(blockEntity.getPos());

        // Faz um raycast do olho da câmera para o centro do bloco.
        // Se acertar um bloco sólido no caminho, a entidade está ocluída.
        var hitResult = world.raycast(new net.minecraft.world.RaycastContext(
                cameraPos,
                blockEntityPos,
                net.minecraft.world.RaycastContext.ShapeType.COLLIDER, // Apenas considera blocos sólidos
                net.minecraft.world.RaycastContext.FluidHandling.NONE,
                MinecraftClient.getInstance().player
        ));

        return hitResult.getType() == net.minecraft.util.hit.HitResult.Type.BLOCK;
    }
    }
}