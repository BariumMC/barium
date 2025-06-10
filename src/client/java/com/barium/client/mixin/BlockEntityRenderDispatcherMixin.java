package com.barium.client.mixin;

import com.barium.client.optimization.ChunkOptimizer;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockEntityRenderer.class)
public abstract class BlockEntityRendererMixin<T extends BlockEntity> {

    @Inject(
        method = "render(Lnet/minecraft/block/entity/BlockEntity;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;II)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void barium$advancedBlockEntityCulling(T entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, CallbackInfo ci) {
        if (MinecraftClient.getInstance().gameRenderer == null) return;
        
        Camera camera = MinecraftClient.getInstance().gameRenderer.getCamera();

        // Estágio 1: Culling por Distância (rápido)
        if (!ChunkOptimizer.shouldRenderBlockEntity(entity, camera)) {
            ci.cancel();
            return;
        }

        // Estágio 2: Culling por Oclusão (mais caro, só é feito se o Estágio 1 passar)
        if (ChunkOptimizer.isBlockEntityOccluded(entity, camera)) {
            ci.cancel();
        }
    }
}