package com.barium.client.mixin;

import com.barium.client.optimization.EntityOptimizer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.ItemFrameEntityRenderer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ItemFrameEntityRenderer.class)
public class ItemFrameEntityRendererMixin {

    // Redirecionamos a chamada para renderizar o item.
    // Isso é mais seguro do que injetar no início do método.
    @Redirect(
        method = "render(Lnet/minecraft/entity/decoration/ItemFrameEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/item/ItemRenderer;renderItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformationMode;IILnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/world/World;I)V")
    )
    private void barium$cullItemFrameContent(ItemRenderer itemRenderer, ItemStack stack, /* ... outros args ... */
                                             Object mode, int light, int overlay, MatrixStack matrices,
                                             VertexConsumerProvider vertexConsumers, Object world, int seed,
                                             // Argumentos do método original para obter o contexto
                                             ItemFrameEntity itemFrameEntity) {
        
        if (EntityOptimizer.shouldRenderItemFrameContent(itemFrameEntity)) {
            // Se estiver perto o suficiente, chamamos o método original para renderizar o item.
            itemRenderer.renderItem(stack, (net.minecraft.client.render.model.json.ModelTransformationMode) mode,
                                    light, overlay, matrices, vertexConsumers, (net.minecraft.world.World) world, seed);
        }
        // Se estiver muito longe, simplesmente não fazemos nada, pulando a renderização do conteúdo.
    }
}