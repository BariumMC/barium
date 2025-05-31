package com.barium.client.mixin;

import com.barium.client.optimization.AnimationCullingOptimizer;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
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
     * Target Method Signature (Yarn 1.21.5+build.1 - ATUALIZADO): render(Lnet/minecraft/block/entity/BlockEntity;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;II)V
     * Target INVOKE (Yarn 1.21.5+build.1 - ATUALIZADO): Lnet/minecraft/client/render/block/entity/BlockEntityRenderer;render(Lnet/minecraft/block/entity/BlockEntity;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IILnet/minecraft/util/math/Vec3d;)V
     */
    @Inject(
        // O MÉTODO A SER INJETADO NO DISPATCHER
        method = "render",
        at = @At(
            value = "INVOKE",
            // O MÉTODO QUE É CHAMADO DENTRO DO DISPATCHER (O RENDERER REAL)
            target = "Lnet/minecraft/client/render/block/entity/BlockEntityRenderer;render(Lnet/minecraft/block/entity/BlockEntity;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IILnet/minecraft/util/math/Vec3d;)V",
            shift = At.Shift.BEFORE
        ),
        cancellable = true
    )
    private <E extends BlockEntity> void barium$beforeRenderAnimatedBlockEntity(
            E blockEntity,
            float tickDelta,
            MatrixStack matrices,
            VertexConsumerProvider vertexConsumers,
            int light,   // Capturado do método do dispatcher
            int overlay, // Capturado do método do dispatcher
            CallbackInfo ci) {

        // Verifica se a animação desta entidade deve ser processada/renderizada
        if (!AnimationCullingOptimizer.shouldAnimateBlockEntity(blockEntity, tickDelta)) {
            // Se shouldAnimateBlockEntity retornar false, cancela a renderização original.
            // Para "pausar" a animação ou renderizá-la de forma diferente, você precisaria
            // chamar o renderizador original com parâmetros modificados (ex: tickDelta = 0)
            // e depois cancelar. Por simplicidade, estamos apenas cancelando aqui.
            ci.cancel();
        }
        // Se shouldAnimateBlockEntity retornar true, a renderização original continua.
    }

    // Nota: Uma abordagem alternativa ou complementar seria injetar no método tick() de BlockEntities
    // específicos (como PistonBlockEntity, BellBlockEntity) para pausar a lógica de animação interna
    // quando não visível, em vez de apenas afetar a renderização.
}