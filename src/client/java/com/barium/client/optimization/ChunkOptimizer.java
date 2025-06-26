package com.barium.client.optimization;

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
     */
    public static boolean isBlockEntityOccluded(BlockEntity blockEntity, Camera camera) {
        if (!BariumConfig.C.ENABLE_BLOCK_ENTITY_OCCLUSION_CULLING) {
            return false;
        }

        var world = blockEntity.getWorld();
        if (world == null) return false;

        Vec3d cameraPos = camera.getPos();
        BlockPos blockEntityBlockPos = blockEntity.getPos();
        Vec3d blockEntityCenterPos = Vec3d.ofCenter(blockEntityBlockPos);
        
        // Otimização: Não faz o raycast para entidades muito próximas
        if (cameraPos.squaredDistanceTo(blockEntityCenterPos) < BariumConfig.C.BLOCK_ENTITY_OCCLUSION_MIN_DISTANCE_SQ) {
            return false;
        }

        // Dispara o raio da câmera para o centro da entidade de bloco
        RaycastContext context = new RaycastContext(
                cameraPos,
                blockEntityCenterPos,
                RaycastContext.ShapeType.COLLIDER, // Usa os colisores dos blocos
                RaycastContext.FluidHandling.NONE,  // Ignora a água
                MinecraftClient.getInstance().player
        );
        BlockHitResult hitResult = world.raycast(context);

        if (hitResult.getType() == HitResult.Type.BLOCK) {
            // Se o raio acertou um bloco DIFERENTE do nosso, então está ocluído.
            return !hitResult.getBlockPos().equals(blockEntityBlockPos);
        }

        // Se errou o alvo (miss) ou acertou uma entidade, não está ocluído.
        return false;
    }
}