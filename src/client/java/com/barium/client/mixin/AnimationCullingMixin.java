package com.barium.client.mixin;

import com.barium.client.optimization.AnimationCullingOptimizer;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin para BlockEntityRenderDispatcher para otimizar animações de Block Entities.
 * Tenta pausar ou reduzir a taxa de atualização de animações para entidades fora de vista ou distantes.
 * Revisado para compatibilidade com mappings Yarn 1.21.5+build.1.
 */
@Mixin(BlockEntityRenderDispatcher.class)
public abstract class AnimationCullingMixin {

    /**
     * Injeta antes da chamada de renderização de um BlockEntity específico.
     * Verifica se a animação desta entidade deve ser renderizada/atualizada.
     *
     * Target Class: net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher
     * Target Method Signature (Yarn 1.21.5+build.1): render(Lnet/minecraft/block/entity/BlockEntity;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;)V
     */
    @Inject(
        method = "render(Lnet/minecraft/block/entity/BlockEntity;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;)V",
        at = @At(
            value = "INVOKE",
            // Target the call to the specific BlockEntityRenderer's render method
            target = "Lnet/minecraft/client/render/block/entity/BlockEntityRenderer;render(Lnet/minecraft/block/entity/BlockEntity;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;II)V",
            shift = At.Shift.BEFORE
        ),
        cancellable = true
    )
    private <E extends BlockEntity> void barium$beforeRenderAnimatedBlockEntity(
            E blockEntity,
            float tickDelta,
            MatrixStack matrices,
            VertexConsumerProvider vertexConsumers,
            CallbackInfo ci,
            // Locals captured by the INVOKE target (adjust based on actual signature)
            BlockEntityRenderer<E> blockEntityRenderer,
            int light,
            int overlay) {

        // Verifica se a animação desta entidade deve ser processada/renderizada
        if (!AnimationCullingOptimizer.shouldAnimateBlockEntity(blockEntity, tickDelta)) {
            // Opção 1: Cancelar completamente a renderização (pode causar pop-in)
            // ci.cancel();

            // Opção 2: Renderizar, mas com um tickDelta modificado (ex: 0 ou um valor fixo)
            // Isso pode congelar a animação no último estado visível.
            // A implementação exata dependeria de como o AnimationCullingOptimizer funciona.
            // Exemplo: Chamar o renderizador original com tickDelta modificado.
            // blockEntityRenderer.render(blockEntity, AnimationCullingOptimizer.getFrozenTickDelta(blockEntity), matrices, vertexConsumers, light, overlay);
            // ci.cancel(); // Cancelar a chamada original após a nossa chamada modificada

            // Por enquanto, vamos apenas cancelar para simplificar. A lógica real precisa ser definida no Optimizer.
            // TODO: Implementar a lógica de culling/pausa no AnimationCullingOptimizer.
            ci.cancel();
        }
        // Se shouldAnimateBlockEntity retornar true, a renderização original continua.
    }

    // Nota: Uma abordagem alternativa ou complementar seria injetar no método tick() de BlockEntities
    // específicos (como PistonBlockEntity, BellBlockEntity) para pausar a lógica de animação interna
    // quando não visível, em vez de apenas afetar a renderização.
}

