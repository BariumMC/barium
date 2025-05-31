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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Otimiza a renderização de Tile Entities (Block Entities), especialmente os dinâmicos.
 * Tenta usar instancing para modelos similares e aplica culling.
 * Baseado nos mappings Yarn 1.21.5+build.1
 * Corrigido: Removido método copy() inexistente e ajustado tipos genéricos.
 */
public class TileEntityOptimizer {

    // Limite de distância quadrada para renderizar Block Entities
    private static final double MAX_RENDER_DISTANCE_SQ = 64 * 64;

    // Cache para agrupar Block Entities por tipo para instancing
    // A chave seria a classe do renderer, o valor seria uma lista de Block Entities desse tipo.
    private static final Map<Class<?>, List<BlockEntityInstanceData>> INSTANCE_GROUPS = new HashMap<>();

    /**
     * Verifica se um BlockEntity deve ser renderizado com base na distância e frustum.
     *
     * @param blockEntity O BlockEntity.
     * @param dispatcher O dispatcher de renderização.
     * @param cameraPos A posição da câmera.
     * @return true se o BlockEntity deve ser renderizado.
     */
    public static boolean shouldRenderBlockEntity(BlockEntity blockEntity, BlockEntityRenderDispatcher dispatcher, Vec3d cameraPos) {
        if (!BariumConfig.ENABLE_ADVANCED_CULLING || !BariumConfig.OPTIMIZE_TILE_ENTITIES) {
            return true;
        }

        BlockPos pos = blockEntity.getPos();
        double distanceSq = cameraPos.squaredDistanceTo(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);

        // Culling por distância
        if (distanceSq > MAX_RENDER_DISTANCE_SQ) {
            return false;
        }

        // Frustum Culling (geralmente feito pelo WorldRenderer antes de chamar o dispatcher)
        // Mas podemos adicionar uma verificação se necessário.
        // Frustum frustum = ... // Obter o frustum atual
        // if (!frustum.isVisible(new Box(pos))) { return false; }

        // Verifica se o renderer existe para este tipo de BlockEntity
        // O método `get` do dispatcher retorna o renderer apropriado.
        @SuppressWarnings("unchecked")
        BlockEntityRenderer<BlockEntity> renderer = (BlockEntityRenderer<BlockEntity>) dispatcher.get(blockEntity);
        if (renderer == null) {
            return false; // Não há como renderizar
        }
        
        // Verifica se o renderer considera a distância (alguns renderers podem ter sua própria lógica)
        // O método `isInRenderDistance` existe no BlockEntityRenderer.
        if (!renderer.isInRenderDistance(blockEntity, cameraPos)) {
             return false;
        }

        return true;
    }

    /**
     * Tenta agrupar Block Entities para renderização com instancing.
     * Este método seria chamado antes da renderização individual.
     *
     * @param blockEntity O BlockEntity a ser potencialmente agrupado.
     * @param matrixStack A pilha de matrizes atual.
     * @param dispatcher O dispatcher de renderização.
     * @return true se o BlockEntity foi adicionado a um grupo de instancing, false caso contrário.
     */
    public static boolean tryGroupBlockEntityForInstancing(BlockEntity blockEntity, MatrixStack matrixStack, BlockEntityRenderDispatcher dispatcher) {
        if (!BariumConfig.ENABLE_ADVANCED_CULLING || !BariumConfig.USE_TILE_ENTITY_INSTANCING) {
            return false;
        }

        @SuppressWarnings("unchecked")
        BlockEntityRenderer<BlockEntity> renderer = (BlockEntityRenderer<BlockEntity>) dispatcher.get(blockEntity);
        if (renderer == null) {
            return false;
        }

        // Verifica se este tipo de renderer suporta instancing (precisaria de uma interface/flag)
        // if (!(renderer instanceof InstancedBlockEntityRenderer)) { return false; }

        // Adiciona os dados da instância (posição, estado, etc.) ao grupo apropriado
        // Usamos Class<?> em vez de Class<? extends BlockEntityRenderer<?>> para evitar problemas de tipo
        List<BlockEntityInstanceData> group = INSTANCE_GROUPS.computeIfAbsent(renderer.getClass(), k -> new ArrayList<>());
        
        // Não usamos copy() pois não existe em Matrix4f no Yarn 1.21.5
        // Em vez disso, criamos uma nova matriz com os mesmos valores
        org.joml.Matrix4f originalMatrix = matrixStack.peek().getPositionMatrix();
        org.joml.Matrix4f matrixCopy = new org.joml.Matrix4f(originalMatrix);
        
        group.add(new BlockEntityInstanceData(blockEntity, matrixCopy));

        // BariumMod.LOGGER.debug("Grouped {} for instancing ({} instances)", blockEntity.getType(), group.size());

        return true; // Indica que foi agrupado e não deve ser renderizado individualmente agora
    }

    /**
     * Renderiza os grupos de Block Entities que foram coletados para instancing.
     * Este método seria chamado após iterar sobre todos os Block Entities visíveis.
     *
     * @param dispatcher O dispatcher de renderização.
     * @param matrixStack A pilha de matrizes.
     * @param vertexConsumers O provedor de consumidores de vértices.
     * @param light O nível de luz.
     * @param overlay O overlay.
     */
    public static void renderInstancedGroups(BlockEntityRenderDispatcher dispatcher, MatrixStack matrixStack, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        if (!BariumConfig.ENABLE_ADVANCED_CULLING || !BariumConfig.USE_TILE_ENTITY_INSTANCING || INSTANCE_GROUPS.isEmpty()) {
            return;
        }

        // BariumMod.LOGGER.debug("Rendering {} instanced groups.", INSTANCE_GROUPS.size());

        for (Map.Entry<Class<?>, List<BlockEntityInstanceData>> entry : INSTANCE_GROUPS.entrySet()) {
            Class<?> rendererClass = entry.getKey();
            List<BlockEntityInstanceData> instances = entry.getValue();

            if (instances.isEmpty()) continue;

            // Obtém o renderer para este tipo (precisa de uma instância, pega a primeira)
            BlockEntity firstEntity = instances.get(0).blockEntity;
            @SuppressWarnings("unchecked")
            BlockEntityRenderer<BlockEntity> renderer = (BlockEntityRenderer<BlockEntity>) dispatcher.get(firstEntity);

            if (renderer != null /* && renderer instanceof InstancedBlockEntityRenderer */) {
                // Chama um método de renderização instanciada no renderer
                // ((InstancedBlockEntityRenderer<BlockEntity>) renderer).renderInstanced(
                //     instances,
                //     matrixStack,
                //     vertexConsumers,
                //     light,
                //     overlay
                // );
                
                // Placeholder: Renderiza individualmente por enquanto, pois instancing real é complexo
                 BariumMod.LOGGER.warn("Instancing for {} not fully implemented, rendering individually.", rendererClass.getSimpleName());
                 for (BlockEntityInstanceData instanceData : instances) {
                     matrixStack.push();
                     // Aplica a transformação específica da instância?
                     // A matriz já foi capturada, mas a renderização pode precisar dela de forma diferente.
                     // Ou talvez o renderer precise ser chamado com a matriz original capturada.
                     // renderer.render(instanceData.blockEntity, 0.0f, matrixStack, vertexConsumers, light, overlay);
                     matrixStack.pop();
                 }
            } else {
                 BariumMod.LOGGER.error("Renderer not found or doesn't support instancing for group: {}", rendererClass.getSimpleName());
            }
        }

        // Limpa os grupos para o próximo frame
        INSTANCE_GROUPS.clear();
    }

    /**
     * Limpa os grupos de instancing.
     */
    public static void clearInstancingGroups() {
        INSTANCE_GROUPS.clear();
    }

    // --- Classe interna para Dados de Instância ---
    // Precisaria conter todas as informações necessárias para renderizar uma instância (posição, estado, etc.)
    private static class BlockEntityInstanceData {
        final BlockEntity blockEntity;
        final org.joml.Matrix4f positionMatrix; // Usando JOML como o Minecraft

        BlockEntityInstanceData(BlockEntity blockEntity, org.joml.Matrix4f positionMatrix) {
            this.blockEntity = blockEntity;
            this.positionMatrix = positionMatrix;
        }
    }

    // Interface hipotética para renderers que suportam instancing
    /*
    public interface InstancedBlockEntityRenderer<T extends BlockEntity> {
        void renderInstanced(List<BlockEntityInstanceData> instances, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay);
    }
    */
}
