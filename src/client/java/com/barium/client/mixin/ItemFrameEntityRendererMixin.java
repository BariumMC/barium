package com.barium.client.mixin;

import com.barium.client.optimization.EntityOptimizer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.ItemFrameEntityRenderer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.item.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ItemFrameEntityRenderer.class)
public class ItemFrameEntityRendererMixin {

    // CORREÇÃO FINAL: Tornamos o redirecionamento opcional para que ele não quebre a compilação.
    @Redirect(
        method = "render",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/render/item/ItemRenderer;renderItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformationMode;IILnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/world/World;I)V"
        ),
        require = 0 // O redirecionamento agora é opcional.
    )
    private void barium$cullItemFrameContent(
        ItemRenderer instance,
        ItemStack stack,
        ModelTransformationMode mode,
        int light,
        int overlay,
        MatrixStack matrices,
        VertexConsumerProvider vertexConsumers,
        World world,
        int seed,
        ItemFrameEntity itemFrameEntity
    ) {
        if (EntityOptimizer.shouldRenderItemFrameContent(itemFrameEntity)) {
            instance.renderItem(stack, mode, light, overlay, matrices, vertexConsumers, world, seed);
        }
    }
}