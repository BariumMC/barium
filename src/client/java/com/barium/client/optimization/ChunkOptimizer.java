package com.barium.client.optimization;

import com.barium.BariumMod;
import com.barium.config.BariumConfig;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Frustum;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

public class ChunkOptimizer {

    // Pontos de teste na bounding box de uma entidade para o raycast de oclusão.
    // Inclui cantos e centros das faces para uma verificação mais robusta.
    private static final Vec3d[] OCCLUSION_TEST_POINTS = new Vec3d[]{
            new Vec3d(0.5, 0.5, 0.5), // Center
            new Vec3d(0.1, 0.1, 0.1), // Corner
            new Vec3d(0.9, 0.1, 0.1), // Corner
            new Vec3d(0.1, 0.9, 0.1), // Corner
            new Vec3d(0.1, 0.1, 0.9), // Corner
            new Vec3d(0.9, 0.9, 0.9)  // Corner
    };

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
     * Verifica se uma entidade de bloco está obstruída.
     * Versão Agressiva: testa múltiplos pontos na bounding box da entidade.
     * Se QUALQUER ponto for visível, a entidade não é considerada ocluída.
     */
    public static boolean isBlockEntityOccluded(BlockEntity blockEntity, Camera camera) {
        if (!BariumConfig.C.ENABLE_BLOCK_ENTITY_OCCLUSION_CULLING) {
            return false;
        }

        World world = blockEntity.getWorld();
        if (world == null) {
            return false;
        }

        Vec3d cameraPos = camera.getPos();
        BlockPos blockEntityBlockPos = blockEntity.getPos();

        // Otimização: Não faz raycast para entidades muito próximas
        if (cameraPos.squaredDistanceTo(Vec3d.ofCenter(blockEntityBlockPos)) < BariumConfig.C.BLOCK_ENTITY_OCCLUSION_MIN_DISTANCE_SQ) {
            return false;
        }

        // Obtém a Bounding Box real da entidade de bloco para o teste.
        // Usamos a posição do bloco como base para a bounding box.
        Box boundingBox = new Box(blockEntityBlockPos);

        // Em modo LLVMpipe, reduzimos os pontos de teste para economizar CPU (um único ponto central).
        Vec3d[] testPoints = OCCLUSION_TEST_POINTS;
        if (BariumConfig.C.ENABLE_LLVMPIPE_MODE) {
            testPoints = new Vec3d[]{ new Vec3d(0.5, 0.5, 0.5) };
        }

        for (Vec3d testPoint : testPoints) {
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

            // Se o raio não atingiu nada, ou se atingiu exatamente o bloco da nossa entidade,
            // então este ponto é visível. A entidade inteira não está ocluída.
            if (hitResult.getType() == HitResult.Type.MISS || hitResult.getBlockPos().equals(blockEntityBlockPos)) {
                return false; // Visível, não ocluir.
            }
        }

