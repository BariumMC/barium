package com.barium.client.optimization;
import com.barium.BariumMod;
import com.barium.config.BariumConfig;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

public class ChunkOptimizer {

private static final Vec3d[] OCCLUSION_TEST_POINTS = new Vec3d[]{
        new Vec3d(0.5, 0.5, 0.5),
        new Vec3d(0.1, 0.1, 0.1),
        new Vec3d(0.9, 0.1, 0.1),
        new Vec3d(0.1, 0.9, 0.1),
        new Vec3d(0.1, 0.1, 0.9),
        new Vec3d(0.9, 0.9, 0.9)
};

public static void init() {
    BariumMod.LOGGER.info("Inicializando ChunkOptimizer");
}

public static boolean shouldRenderBlockEntity(BlockEntity blockEntity, Camera camera) {
    if (!BariumConfig.C.ENABLE_BLOCK_ENTITY_CULLING) {
        return true;
    }
    Vec3d blockEntityPos = Vec3d.ofCenter(blockEntity.getPos());
    // CORREÇÃO FINAL 25w45a: O método correto é getPos().
    Vec3d cameraPos = camera.getPos();
    double distanceSq = blockEntityPos.squaredDistanceTo(cameraPos);
    return distanceSq <= BariumConfig.C.MAX_BLOCK_ENTITY_RENDER_DISTANCE_SQ;
}

public static boolean isBlockEntityOccluded(BlockEntity blockEntity, Camera camera) {
    if (!BariumConfig.C.ENABLE_BLOCK_ENTITY_OCCLUSION_CULLING) {
        return false;
    }

    World world = blockEntity.getWorld();
    if (world == null) {
        return false;
    }

    // CORREÇÃO FINAL 25w45a: O método correto é getPos().
    Vec3d cameraPos = camera.getPos();
    BlockPos blockEntityBlockPos = blockEntity.getPos();

    if (cameraPos.squaredDistanceTo(Vec3d.ofCenter(blockEntityBlockPos)) < BariumConfig.C.BLOCK_ENTITY_OCCLUSION_MIN_DISTANCE_SQ) {
        return false;
    }

    Box boundingBox = new Box(blockEntityBlockPos);

    for (Vec3d testPoint : OCCLUSION_TEST_POINTS) {
        Vec3d targetPos = new Vec3d(
            boundingBox.minX + (boundingBox.maxX - boundingBox.minX) * testPoint.x,
            boundingBox.minY + (boundingBox.maxY - boundingBox.minY) * testPoint.y,
            boundingBox.minZ + (boundingBox.maxZ - boundingBox.minZ) * testPoint.z
        );

        RaycastContext context = new RaycastContext(
                cameraPos,
                targetPos,
                RaycastContext.ShapeType.COLLIDER,
                RaycastContext.FluidHandling.NONE,
                MinecraftClient.getInstance().player
        );
        BlockHitResult hitResult = world.raycast(context);

        if (hitResult.getType() == HitResult.Type.MISS || hitResult.getBlockPos().equals(blockEntityBlockPos)) {
            return false;
        }
    }

    return true;
}