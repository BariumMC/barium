package com.barium.client.mixin;

import com.barium.client.optimization.EntityOptimizer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin<T extends Entity> {

    // CORREÇÃO: Usamos @Shadow para obter uma referência direta ao campo 'dispatcher'.
    // A anotação @Final é necessária porque o campo original é final.
    // Isso nos dá acesso direto e seguro, respeitando as regras do Mixin.
    @Shadow @Final protected EntityRenderDispatcher dispatcher;

    @Inject(
        method = "render",
        at = @At("HEAD"),
        cancellable = true
    )
    private void barium$cullDistantEntity(T entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        
        // A câmera pode ser nula durante a inicialização.
        // Agora podemos acessar 'this.dispatcher' diretamente graças ao @Shadow.
        if (this.dispatcher.camera == null) {
            return;
        }
        
        // Usamos a referência direta aqui também.
        if (!EntityOptimizer.shouldRenderEntity(entity, this.dispatcher.camera)) {
            ci.cancel();
        }
    }
}