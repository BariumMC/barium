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
        // CORREÇÃO: Usamos um cast de tipo bruto para evitar o erro de compilação "wrong number of type arguments".
        // Isso nos dá acesso ao dispatcher sem o compilador reclamar sobre os genéricos.
        EntityRenderer self = (EntityRenderer)(Object)this;
        
        // A câmera pode ser nula durante a inicialização
        if (self.getDispatcher().camera == null) {
            return;
        }
        
        if (!EntityOptimizer.shouldRenderEntity(entity, self.getDispatcher().camera)) {
            ci.cancel();
        }
    }
}