package com.barium.client.optimization;

import com.barium.config.BariumConfig;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

public class ChunkOptimizer {

    public static boolean shouldRenderBlockEntity(BlockEntity blockEntity, Camera camera) {
        if (!BariumConfig.C.ENABLE_BLOCK_ENTITY_CULLING) return true;
        
        double distSq = blockEntity.getPos().getSquaredDistance(camera.getPos());
        return distSq <= BariumConfig.C.MAX_BLOCK_ENTITY_RENDER_DISTANCE_SQ;
    }

    public static boolean isBlockEntityOccluded(BlockEntity blockEntity, Camera camera) {
        if (!BariumConfig.C.ENABLE_BLOCK_ENTITY_OCCLUSION_CULLING) return false;

        // Não aplicar oclusão em baús para evitar bugs de invisibilidade
        if (blockEntity instanceof ChestBlockEntity) return false;

        World world = blockEntity.getWorld();
        if (world == null) return false;

        BlockPos pos = blockEntity.getPos();
        Vec3d cameraPos = camera.getPos();
        
        // Se estiver muito perto (menos de 4 blocos), não ocluir para evitar bugs visuais
        if (cameraPos.squaredDistanceTo(Vec3d.ofCenter(pos)) < 16.0) return false;

        // O segredo: Raycast tipo VISUAL e ignorar o próprio bloco atingido
        RaycastContext context = new RaycastContext(
                cameraPos, 
                Vec3d.ofCenter(pos),
                RaycastContext.ShapeType.VISUAL, 
                RaycastContext.FluidHandling.NONE, 
                MinecraftClient.getInstance().player
        );

        BlockHitResult hitResult = world.raycast(context);

        // Se o hit for do tipo MISS ou se o bloco atingido for o próprio baú, ele DEVE renderizar
        if (hitResult.getType() == HitResult.Type.MISS) return false;
        if (hitResult.getBlockPos().equals(pos)) return false;

        // Se o raio bateu em outro bloco antes, então o baú está escondido
        return true;
    }
}
