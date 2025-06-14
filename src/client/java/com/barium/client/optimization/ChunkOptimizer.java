package com.barium.client.optimization;

import com.barium.BariumMod;
import com.barium.config.BariumConfig;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

public class ChunkOptimizer {

    public static void init() {
        BariumMod.LOGGER.info("Inicializando ChunkOptimizer");
    }

    public static boolean shouldRenderBlockEntity(BlockEntity blockEntity, Camera camera) {
        if (!BariumConfig.C.ENABLE_BLOCK_ENTITY_CULLING) {
            return true;
        }
        Vec3d blockEntityPos = Vec3d.ofCenter(blockEntity.getPos());
        Vec3d cameraPos = camera.getPos();
        double distanceSq = blockEntityPos.squaredDistanceTo(cameraPos);
        return distanceSq <= BariumConfig.C.MAX_BLOCK_ENTITY_RENDER_DISTANCE_SQ;
    }

    /**
     * Verifica se uma entidade de bloco está obstruída por outra geometria do mundo.
     * Esta é a versão CORRIGIDA e OTIMIZADA.
     */
    public static boolean isBlockEntityOccluded(BlockEntity blockEntity, Camera camera) {
        // 1. Checa se a otimização está ligada
        if (!BariumConfig.C.ENABLE_BLOCK_ENTITY_OCCLUSION_CULLING) {
            return false;
        }

        var world = blockEntity.getWorld();
        if (world == null) {
            return false; // Não podemos fazer nada sem um mundo
        }

        Vec3d cameraPos = camera.getPos();
        BlockPos blockEntityBlockPos = blockEntity.getPos();
        Vec3d blockEntityCenterPos = Vec3d.ofCenter(blockEntityBlockPos);
        
        // 2. OTIMIZAÇÃO: Não faz o raycast para entidades muito próximas
        double distanceSq = cameraPos.squaredDistanceTo(blockEntityCenterPos);
        if (distanceSq < BariumConfig.C.BLOCK_ENTITY_OCCLUSION_MIN_DISTANCE_SQ) {
            return false;
        }

        // 3. Dispara o raio da câmera para o centro da entidade de bloco
        RaycastContext context = new RaycastContext(
                cameraPos,
                blockEntityCenterPos,
                RaycastContext.ShapeType.COLLIDER, // Usa os colisores dos blocos
                RaycastContext.FluidHandling.NONE,  // Ignora a água
                MinecraftClient.getInstance().player
        );
        BlockHitResult hitResult = world.raycast(context);

        // 4. A GRANDE CORREÇÃO: Analisa o resultado do raio
        if (hitResult.getType() == HitResult.Type.MISS) {
            // O raio não acertou nada, então o caminho está livre. Não está ocluído.
            return false;
        }
        
        if (hitResult.getType() == HitResult.Type.BLOCK) {
            // O raio acertou um bloco. Agora precisamos saber QUAL bloco.
            BlockPos hitBlockPos = hitResult.getBlockPos();
            
            // Se a posição do bloco que o raio acertou for DIFERENTE da posição
            // da nossa entidade de bloco, significa que há algo no caminho.
            // Portanto, a entidade ESTÁ ocluída.
            return !hitBlockPos.equals(blockEntityBlockPos);
        }

        // Caso padrão: não ocluído.
        return false;
    }
}