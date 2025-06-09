package com.barium.client.mixin;

import com.barium.client.optimization.EntityOptimizer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.ItemFrameEntityRenderer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode; // Importamos a classe principal
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ItemFrameEntityRenderer.class)
public class ItemFrameEntityRendererMixin {

    @Redirect(
        // Usar o nome do método em vez da assinatura completa é mais robusto
        method = "render",
        at = @At(
            value = "INVOKE",
            // A assinatura do target do método de renderização do item
            target = "Lnet/minecraft/client/render/item/ItemRenderer;renderItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformationMode;IILnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/world/World;I)V"
        )
    )
    private void barium$cullItemFrameContent(
        // Argumentos do método original que estamos redirecionando
        ItemRenderer instance, // A instância do ItemRenderer
        ItemStack stack,
        ModelTransformationMode mode,
        int light,
        int overlay,
        MatrixStack matrices,
        VertexConsumerProvider vertexConsumers,
        World world,
        int seed,
        // Argumentos do método 'render' original para obter contexto
        ItemFrameEntity itemFrameEntity // O contexto da entidade
    ) {
        
        if (EntityOptimizer.shouldRenderItemFrameContent(itemFrameEntity)) {
            // Se estiver perto o suficiente, chamamos o método original com os argumentos corretos.
            // O cast agora é para ModelTransformationMode.Mode
            instance.renderItem(stack, mode, light, overlay, matrices, vertexConsumers, world, seed);
        }
        // Se estiver muito longe, simplesmente não fazemos nada, pulando a renderização do conteúdo.
    }
}