package com.barium.client.mixin;

import com.barium.client.optimization.EntityOptimizer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin<T extends Entity> {

    @Inject(
        method = "render",
        at = @At("HEAD"),
        cancellable = true
    )
    private void barium$cullDistantEntity(T entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        // Usamos um cast de tipo bruto para evitar problemas com genéricos do compilador.
        EntityRenderer self = (EntityRenderer)(Object)this;
        
        // CORREÇÃO: Acessamos 'dispatcher' como um campo, não como um método getDispatcher().
        if (self.dispatcher.camera == null) {
            return;
        }
        
        // CORREÇÃO: Acessamos 'dispatcher' como um campo aqui também.
        if (!EntityOptimizer.shouldRenderEntity(entity, self.dispatcher.camera)) {
            ci.cancel();
        }
    }
}