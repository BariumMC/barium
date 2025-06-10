package com.barium.client.optimization;

import com.barium.BariumMod;
import com.barium.config.BariumConfig;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.util.hit.HitResult;

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

        return distanceSq <= BariumConfig.MAX_BLOCK_ENTITY_RENDER_DISTANCE_SQ;
    }

    /**
     * CORREÇÃO: Este método agora está DENTRO da classe ChunkOptimizer.
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
        var hitResult = world.raycast(new RaycastContext(
                cameraPos,
                blockEntityPos,
                RaycastContext.ShapeType.COLLIDER, // Apenas considera blocos sólidos
                RaycastContext.FluidHandling.NONE,
                MinecraftClient.getInstance().player
        ));

        return hitResult.getType() == HitResult.Type.BLOCK;
    }

} // A chave de fechamento final da classe