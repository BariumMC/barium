package com.barium.client.mixin;

import com.barium.client.optimization.ChunkOptimizer;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.Camera;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.VertexConsumerProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockEntityRenderer.class)
public abstract class BlockEntityRendererMixin<T extends BlockEntity> {

    // Injeta no início do método de renderização de entidade de bloco
    // e cancela a renderização se a entidade não deve ser visível.
    @Inject(
        method = "render(Lnet/minecraft/block/entity/BlockEntity;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;II)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void barium$cullBlockEntityRender(T blockEntity, float tickDelta, MatrixStack matrices,
                                              VertexConsumerProvider vertexConsumers, int light, int overlay,
                                              CallbackInfo ci) {
        Camera camera = MinecraftClient.getInstance().gameRenderer.getCamera();

        if (!ChunkOptimizer.shouldRenderBlockEntity(blockEntity, camera)) {
            ci.cancel(); // Cancela a renderização se a entidade deve ser ocultada
        }
    }
}
