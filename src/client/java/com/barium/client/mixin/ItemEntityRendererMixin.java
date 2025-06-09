package com.barium.client.mixin;

import com.barium.client.optimization.EntityOptimizer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.ItemEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntityRenderer.class)
public class ItemEntityRendererMixin {

    // Marcamos o início da renderização de um item no chão
    @Inject(method = "render", at = @At("HEAD"))
    private void barium$beforeRenderItem(ItemEntity itemEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci) {
        EntityOptimizer.isRenderingDroppedItem = true;
        EntityOptimizer.currentlyRenderingEntity = itemEntity;
    }

    // Marcamos o fim da renderização para não afetar outros itens (ex: na GUI)
    @Inject(method = "render", at = @At("RETURN"))
    private void barium$afterRenderItem(ItemEntity itemEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci) {
        EntityOptimizer.isRenderingDroppedItem = false;
        EntityOptimizer.currentlyRenderingEntity = null;
    }
}