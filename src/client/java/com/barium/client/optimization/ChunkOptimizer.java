package com.barium.client.optimization;

import com.barium.BariumMod;
import com.barium.config.ConfigData;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

public class ChunkOptimizer {

    public static void init() {
        BariumMod.LOGGER.info("Inicializando ChunkOptimizer");
    }

    public static boolean shouldRenderBlockEntity(BlockEntity blockEntity, Camera camera) {
        if (!BariumConfig.ENABLE_BLOCK_ENTITY_CULLING) {
            return true;
        }
        Vec3d blockEntityPos = Vec3d.ofCenter(blockEntity.getPos());
        Vec3d cameraPos = camera.getPos();
        double distanceSq = blockEntityPos.squaredDistanceTo(cameraPos);
        return distanceSq <= BariumConfig.MAX_BLOCK_ENTITY_RENDER_DISTANCE_SQ;
    }

    public static boolean isBlockEntityOccluded(BlockEntity blockEntity, Camera camera) {
        if (!BariumConfig.ENABLE_BLOCK_ENTITY_OCCLUSION_CULLING) {
            return false;
        }
        var world = blockEntity.getWorld();
        if (world == null) return false;

        Vec3d cameraPos = camera.getPos();
        Vec3d blockEntityPos = Vec3d.ofCenter(blockEntity.getPos());

        var hitResult = world.raycast(new RaycastContext(
                cameraPos,
                blockEntityPos,
                RaycastContext.ShapeType.COLLIDER,
                RaycastContext.FluidHandling.NONE,
                MinecraftClient.getInstance().player
        ));
        return hitResult.getType() == HitResult.Type.BLOCK;
    }
}