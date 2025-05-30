package com.barium.client.optimization;

import com.barium.BariumMod;
import com.barium.config.BariumConfig;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.client.render.Camera; // Adicionado para processVisibleBlockEntities

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TileEntityOptimizer {

    private static final double MAX_RENDER_DISTANCE_SQ = 64 * 64;
    private static final Map<Class<?>, List<BlockEntityInstanceData>> INSTANCE_GROUPS = new HashMap<>();

    public static boolean shouldRenderBlockEntity(BlockEntity blockEntity, BlockEntityRenderDispatcher dispatcher, Vec3d cameraPos) {
        if (!BariumConfig.ENABLE_ADVANCED_CULLING || !BariumConfig.OPTIMIZE_TILE_ENTITIES) {
            return true;
        }

        BlockPos pos = blockEntity.getPos();
        double distanceSq = cameraPos.squaredDistanceTo(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);

        if (distanceSq > MAX_RENDER_DISTANCE_SQ) {
            return false;
        }

        @SuppressWarnings("unchecked")
        BlockEntityRenderer<BlockEntity> renderer = (BlockEntityRenderer<BlockEntity>) dispatcher.get(blockEntity);
        if (renderer == null) {
            return false;
        }
        
        if (!renderer.isInRenderDistance(blockEntity, cameraPos)) {
             return false;
        }

        return true;
    }

    // NOVO MÉTODO para ser chamado pelo mixin de WorldRenderer
    public static boolean processVisibleBlockEntities(List<BlockEntity> visibleBlockEntities, BlockEntityRenderDispatcher dispatcher, MatrixStack matrices, VertexConsumerProvider vertexConsumers, Camera camera, float tickDelta) {
        if (!BariumConfig.ENABLE_ADVANCED_CULLING || !BariumConfig.OPTIMIZE_TILE_ENTITIES) {
            return false; // Não otimiza, permite que o vanilla renderize
        }

        clearInstancingGroups(); // Limpa grupos do frame anterior

        for (BlockEntity blockEntity : visibleBlockEntities) {
            // Verifica culling adicional antes de agrupar
            if (shouldRenderBlockEntity(blockEntity, dispatcher, camera.getPos())) {
                tryGroupBlockEntityForInstancing(blockEntity, matrices, dispatcher);
            }
        }

        // Renderiza todos os grupos instanciados
        renderInstancedGroups(dispatcher, matrices, vertexConsumers, 15728880, 65536); // Exemplo de light/overlay

        // Retorna true se a otimização foi aplicada e a renderização vanilla deve ser ignorada.
        return true; // Se o otimizador vai lidar com tudo, retorna true.
    }


    public static boolean tryGroupBlockEntityForInstancing(BlockEntity blockEntity, MatrixStack matrixStack, BlockEntityRenderDispatcher dispatcher) {
        if (!BariumConfig.ENABLE_ADVANCED_CULLING || !BariumConfig.USE_TILE_ENTITY_INSTANCING) {
            return false;
        }

        @SuppressWarnings("unchecked")
        BlockEntityRenderer<BlockEntity> renderer = (BlockEntityRenderer<BlockEntity>) dispatcher.get(blockEntity);
        if (renderer == null) {
            return false;
        }

        List<BlockEntityInstanceData> group = INSTANCE_GROUPS.computeIfAbsent(renderer.getClass(), k -> new ArrayList<>());
        
        org.joml.Matrix4f originalMatrix = matrixStack.peek().getPositionMatrix();
        org.joml.Matrix4f matrixCopy = new org.joml.Matrix4f(originalMatrix); // Correção: Usar construtor de cópia JOML
        
        group.add(new BlockEntityInstanceData(blockEntity, matrixCopy));

        return true;
    }

    public static void renderInstancedGroups(BlockEntityRenderDispatcher dispatcher, MatrixStack matrixStack, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        if (!BariumConfig.ENABLE_ADVANCED_CULLING || !BariumConfig.USE_TILE_ENTITY_INSTANCING || INSTANCE_GROUPS.isEmpty()) {
            return;
        }

        BariumMod.LOGGER.debug("Rendering {} instanced groups.", INSTANCE_GROUPS.size());

        for (Map.Entry<Class<?>, List<BlockEntityInstanceData>> entry : INSTANCE_GROUPS.entrySet()) {
            Class<?> rendererClass = entry.getKey();
            List<BlockEntityInstanceData> instances = entry.getValue();

            if (instances.isEmpty()) continue;

            BlockEntity firstEntity = instances.get(0).blockEntity;
            @SuppressWarnings("unchecked")
            BlockEntityRenderer<BlockEntity> renderer = (BlockEntityRenderer<BlockEntity>) dispatcher.get(firstEntity);

            if (renderer != null /* && renderer instanceof InstancedBlockEntityRenderer */) {
                 BariumMod.LOGGER.warn("Instancing for {} not fully implemented, rendering individually.", rendererClass.getSimpleName());
                 for (BlockEntityInstanceData instanceData : instances) {
                     matrixStack.push();
                     // AQUI: Você precisaria aplicar instanceData.positionMatrix ao matrices
                     // Isso geralmente envolve setando a matriz diretamente ou multiplicando-a
                     // Por simplicidade para placeholder:
                     renderer.render(instanceData.blockEntity, 0.0f, matrixStack, vertexConsumers, light, overlay);
                     matrixStack.pop();
                 }
            } else {
                 BariumMod.LOGGER.error("Renderer not found or doesn't support instancing for group: {}", rendererClass.getSimpleName());
            }
        }
        clearInstancingGroups(); // Limpa os grupos para o próximo frame
    }

    public static void clearInstancingGroups() {
        INSTANCE_GROUPS.clear();
    }

    private static class BlockEntityInstanceData {
        final BlockEntity blockEntity;
        final org.joml.Matrix4f positionMatrix;

        BlockEntityInstanceData(BlockEntity blockEntity, org.joml.Matrix4f positionMatrix) {
            this.blockEntity = blockEntity;
            this.positionMatrix = positionMatrix;
        }
    }
}