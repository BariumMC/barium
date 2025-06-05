package com.barium.client.mixin;

import com.barium.client.optimization.EntityRenderOptimizer; // Importar o otimizador
import net.minecraft.client.render.entity.EntityRenderer; // A classe genérica EntityRenderer
import net.minecraft.client.render.Camera;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.client.util.math.MatrixStack; // Para a assinatura do método render
import net.minecraft.client.render.VertexConsumerProvider; // Para a assinatura do método render
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin para a classe EntityRenderer, otimizando a renderização de entidades.
 * Aplica culling por distância e frustum de visão.
 */
@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin<T extends Entity> {

    // A assinatura do método render para EntityRenderer é geralmente:
    // render(Lnet/minecraft/entity/Entity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V
    // Onde o terceiro float é o partialTicks (delta).
    // O quinto argumento é MatrixStack, o sexto VertexConsumerProvider, o sétimo LightmapTextureManager.

    @Inject(
        method = "render(Lnet/minecraft/entity/Entity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void barium$cullEntityRender(T entity, float yaw, float tickDelta, MatrixStack matrices,
                                         VertexConsumerProvider vertexConsumers, int light,
                                         CallbackInfo ci) {
        Camera camera = MinecraftClient.getInstance().gameRenderer.getCamera();

        if (!EntityRenderOptimizer.shouldRenderEntity(entity, camera)) {
            ci.cancel(); // Cancela a renderização se a entidade deve ser ocultada
        }
    }
}