package com.barium.client.mixin;

import com.barium.client.optimization.AnimationCullingOptimizer;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
// import net.minecraft.client.render.block.entity.BlockEntityRenderer; // Não é necessário como parâmetro aqui
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockEntityRenderDispatcher.class)
public abstract class AnimationCullingMixin {

    @Inject(
        method = "render(Lnet/minecraft/block/entity/BlockEntity;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;)V",
        at = @At(
            value = "INVOKE",
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
            CallbackInfo ci) { // Removidos blockEntityRenderer, light, overlay daqui

        if (!AnimationCullingOptimizer.shouldAnimateBlockEntity(blockEntity, tickDelta)) {
            ci.cancel();
        }
    }
}