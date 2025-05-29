package com.barium.client.optimization;

import com.barium.BariumMod;
import com.barium.config.BariumConfig;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.Camera;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix3f; // Import para Matrix3f
import org.joml.Matrix4f; // Import para Matrix4f

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Otimiza a renderização de Tile Entities (Block Entities), especialmente os dinâmicos.
 * Tenta usar instancing para modelos similares e aplica culling.
 * Baseado nos mappings Yarn 1.21.5+build.1
 * Corrigido: Ajustado para nova injeção em TileEntityMixin e uso de JOML Matrix4f.
 * Corrigido: Chamada a render() em BlockEntityRenderer com Vec3d modelOffset.
 * Corrigido: Uso correto de matrices.multiply(Matrix4f, Matrix3f).
 */
public class TileEntityOptimizer {

    // Limite de distância quadrada para renderizar Block Entities
    private static final double MAX_RENDER_DISTANCE_SQ = 64 * 64;

    // Cache para agrupar Block Entities por tipo para instancing
    private static final Map<Class<?>, List<BlockEntityInstanceData>> INSTANCE_GROUPS = new HashMap<>();

    /**
     * Verifica se um BlockEntity deve ser renderizado com base na distância e frustum.
     *
     * @param blockEntity O BlockEntity.
     * @param dispatcher O dispatcher de renderização.
     * @param camera A câmera atual.
     * @return true se o BlockEntity deve ser renderizado.
     */
    public static boolean shouldRenderBlockEntity(BlockEntity blockEntity, BlockEntityRenderDispatcher dispatcher, Camera camera) {
        if (!BariumConfig.ENABLE_ADVANCED_CULLING || !BariumConfig.OPTIMIZE_TILE_ENTITIES) {
            return true;
        }

        BlockPos pos = blockEntity.getPos();
        Vec3d cameraPos = camera.getPos();
        double distanceSq = cameraPos.squaredDistanceTo(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);

        // Culling por distância
        if (distanceSq > MAX_RENDER_DISTANCE_SQ) {
            return false;
        }

        @SuppressWarnings("unchecked")
        BlockEntityRenderer<BlockEntity> renderer = (BlockEntityRenderer<BlockEntity>) dispatcher.get(blockEntity);
        if (renderer == null) {
            return false; // Não há como renderizar
        }
        
        if (!renderer.isInRenderDistance(blockEntity, cameraPos)) {
             return false;
        }

        return true;
    }

    /**
     * Tenta agrupar Block Entities para renderização com instancing.
     * Este método seria chamado para cada BlockEntity visível.
     *
     * @param blockEntity O BlockEntity a ser potencialmente agrupado.
     * @param dummyMatrices Uma MatrixStack (pode ser temporária) usada para copiar a matriz da entidade.
     * @param dispatcher O dispatcher de renderização.
     * @return true se o BlockEntity foi adicionado a um grupo de instancing ou culled, false caso contrário.
     */
    public static boolean tryGroupBlockEntityForInstancing(BlockEntity blockEntity, MatrixStack dummyMatrices, BlockEntityRenderDispatcher dispatcher) {
        if (!BariumConfig.ENABLE_ADVANCED_CULLING || !BariumConfig.USE_TILE_ENTITY_INSTANCING) {
            return false;
        }

        Camera camera = MinecraftClient.getInstance().gameRenderer.getCamera();
        if (!shouldRenderBlockEntity(blockEntity, dispatcher, camera)) {
            return true; // Já culled, então marca como "tratado"
        }

        @SuppressWarnings("unchecked")
        BlockEntityRenderer<BlockEntity> renderer = (BlockEntityRenderer<BlockEntity>) dispatcher.get(blockEntity);
        if (renderer == null) {
            return false;
        }

        List<BlockEntityInstanceData> group = INSTANCE_GROUPS.computeIfAbsent(renderer.getClass(), k -> new ArrayList<>());
        
        org.joml.Matrix4f instanceModelMatrix = new org.joml.Matrix4f();
        BlockPos pos = blockEntity.getPos();
        instanceModelMatrix.translate(pos.getX(), pos.getY(), pos.getZ());
        
        group.add(new BlockEntityInstanceData(blockEntity, instanceModelMatrix));

        return true; // Indica que foi agrupado e não deve ser renderizado individualmente agora
    }

    /**
     * Renderiza os grupos de Block Entities que foram coletados para instancing.
     * Este método seria chamado após iterar sobre todos os Block Entities visíveis.
     *
     * @param dispatcher O dispatcher de renderização.
     * @param matrices A pilha de matrizes global do WorldRenderer (usada para o contexto de renderização).
     * @param vertexConsumers O provedor de consumidores de vértices.
     * @param light O nível de luz.
     * @param overlay O overlay.
     */
    public static void renderInstancedGroups(BlockEntityRenderDispatcher dispatcher, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        if (!BariumConfig.ENABLE_ADVANCED_CULLING || !BariumConfig.USE_TILE_ENTITY_INSTANCING || INSTANCE_GROUPS.isEmpty()) {
            return;
        }

        for (Map.Entry<Class<?>, List<BlockEntityInstanceData>> entry : INSTANCE_GROUPS.entrySet()) {
            Class<?> rendererClass = entry.getKey();
            List<BlockEntityInstanceData> instances = entry.getValue();

            if (instances.isEmpty()) continue;

            BlockEntity firstEntity = instances.get(0).blockEntity;
            @SuppressWarnings("unchecked")
            BlockEntityRenderer<BlockEntity> renderer = (BlockEntityRenderer<BlockEntity>) dispatcher.get(firstEntity);

            if (renderer != null) {
                BariumMod.LOGGER.warn("Instancing for {} not fully implemented, rendering individually.", rendererClass.getSimpleName());
                 for (BlockEntityInstanceData instanceData : instances) {
                     matrices.push();
                     
                     // Multiplica a matriz global atual pela matriz de posição da instância.
                     // A BlockEntityRenderer.render espera a matriz de modelo-visão correta.
                     // Em 1.21.5, o método é multiply(Matrix4f, Matrix3f)
                     matrices.multiply(instanceData.positionMatrix, new Matrix3f()); // Use Matrix3f de identidade

                     // Renderiza a entidade individualmente.
                     // Assinatura em 1.21.5: render(T blockEntity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, Vec3d modelOffset)
                     renderer.render(instanceData.blockEntity, 0.0f, matrices, vertexConsumers, light, overlay, Vec3d.ZERO); // Adiciona Vec3d.ZERO

                     matrices.pop();
                 }
            } else {
                 BariumMod.LOGGER.error("Renderer not found or doesn't support instancing for group: {}", rendererClass.getSimpleName());
            }
        }

        INSTANCE_GROUPS.clear();
    }

    /**
     * Limpa os grupos de instancing.
     */
    public static void clearInstancingGroups() {
        INSTANCE_GROUPS.clear();
    }

    // --- Classe interna para Dados de Instância ---
    private static class BlockEntityInstanceData {
        final BlockEntity blockEntity;
        final Matrix4f positionMatrix; // Usando JOML como o Minecraft

        BlockEntityInstanceData(BlockEntity blockEntity, Matrix4f positionMatrix) {
            this.blockEntity = blockEntity;
            this.positionMatrix = positionMatrix;
        }
    }
}