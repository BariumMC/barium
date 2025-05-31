package com.barium.client.mixin;

import com.barium.client.optimization.TileEntityOptimizer;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.BufferBuilderStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;
import java.util.Map;
import java.util.Queue;

/**
 * Mixin para WorldRenderer para otimizar a renderização de Block Entities (Tile Entities).
 * Foca em aplicar instancing ou batching antes da renderização individual.
 * Revisado para compatibilidade com mappings Yarn 1.21.5+build.1.
 * Corrigido: Comentada injeção com assinatura incerta para evitar erros de compilação.
 */
@Mixin(WorldRenderer.class)
public abstract class TileEntityMixin {

    @Shadow private BufferBuilderStorage bufferBuilders;
    @Shadow private BlockEntityRenderDispatcher blockEntityRenderDispatcher;

    /**
     * Injeta no método que renderiza block entities para aplicar otimizações como instancing.
     * Captura a fila de block entities visíveis antes que sejam renderizadas individualmente.
     *
     * Target Class: net.minecraft.client.render.WorldRenderer
     * Target Method Signature (Yarn 1.21.5+build.1): renderBlockEntities(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;Lnet/minecraft/client/render/Camera;F)V
     * AVISO: Assinatura precisa ser verificada nos mappings Yarn 1.21.5+build.1.
     * Comentado para evitar erros de compilação até que a assinatura correta seja confirmada.
     */
    /*
    @Inject(
        method = "renderBlockEntities(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;Lnet/minecraft/client/render/Camera;F)V",
        at = @At(
            value = "INVOKE",
            // Target the point where the dispatcher is about to render an individual entity
            target = "Lnet/minecraft/client/render/block/entity/BlockEntityRenderDispatcher;render(Lnet/minecraft/block/entity/BlockEntity;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;)V",
            shift = At.Shift.BEFORE // Inject before the first individual render call
        ),
        locals = LocalCapture.CAPTURE_FAILSOFT, // Capture local variables like the queue/list of entities
        cancellable = true
    )
    private void barium$optimizeBlockEntityRendering(
            MatrixStack matrices,
            VertexConsumerProvider.Immediate vertexConsumers,
            Camera camera,
            float tickDelta,
            CallbackInfo ci,
            // Captured locals (names and types need verification based on actual compiled code/mappings)
            Queue<BlockEntity> visibleBlockEntitiesQueue // Assuming a Queue is used locally
            // other locals might be captured here
    ) {
        // Convert the queue/collection of visible entities to a list
        // Note: The actual local variable holding the entities might be different (e.g., a List, Set)
        List<BlockEntity> visibleBlockEntities = List.copyOf(visibleBlockEntitiesQueue);

        // Prepara as tile entities para renderização otimizada (grouping, etc.)
        Map<Class<? extends BlockEntity>, List<BlockEntity>> groupedEntities =
                TileEntityOptimizer.prepareForRendering(visibleBlockEntities);

        if (groupedEntities != null && !groupedEntities.isEmpty()) {
            // Renderiza cada grupo de tile entities com otimizações (e.g., instancing)
            for (Map.Entry<Class<? extends BlockEntity>, List<BlockEntity>> entry : groupedEntities.entrySet()) {
                TileEntityOptimizer.renderEntitiesByType(
                    entry.getKey(),
                    entry.getValue(),
                    this.blockEntityRenderDispatcher, // Use the dispatcher from WorldRenderer
                    matrices,
                    vertexConsumers,
                    camera, // Pass camera if needed by optimizer
                    tickDelta // Pass tickDelta if needed
                    // light and overlay might need to be obtained differently or passed
                );
            }

            // Cancela o restante do método original, pois já renderizamos tudo de forma otimizada
            ci.cancel();
        }
        // Se groupedEntities for nulo ou vazio, permite que a renderização original continue.
    }
    */

    // TODO: Confirmar a assinatura exata do método `renderBlockEntities` em Yarn 1.21.5+build.1.
    // TODO: Implementar a lógica detalhada em TileEntityOptimizer (prepareForRendering, renderEntitiesByType).
    // TODO: Garantir que a captura de `visibleBlockEntitiesQueue` (ou similar) funcione corretamente.
}
