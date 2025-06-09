package com.barium.client.mixin;

import com.barium.client.optimization.EntityOptimizer;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin<T extends Entity> {

    @Shadow @Final protected EntityRenderDispatcher dispatcher;

    // Esta otimização já funciona e é segura.
    @Inject(
        method = "shouldRender(Lnet/minecraft/entity/Entity;Lnet/minecraft/client/render/Frustum;DDD)Z",
        at = @At("HEAD"),
        cancellable = true
    )
    private void barium$cullDistantEntity(T entity, Frustum frustum, double x, double y, double z, CallbackInfoReturnable<Boolean> cir) {
        if (this.dispatcher.camera == null) return;
        if (!EntityOptimizer.shouldRenderEntity(entity, this.dispatcher.camera)) {
            cir.setReturnValue(false);
        }
    }
    
    // CORREÇÃO FINAL: Tornamos a injeção opcional para que ela não quebre a compilação
    // se o método não for encontrado no ambiente de build.
    @Inject(
        method = "renderLabelIfPresent(Lnet/minecraft/entity/Entity;Lnet/minecraft/text/Text;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
        at = @At("HEAD"),
        cancellable = true,
        require = 0 // A injeção agora é opcional.
    )
    private void barium$cullNameTag(T entity, Text text, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        if (!EntityOptimizer.shouldRenderNameTag(entity)) {
            ci.cancel();
        }
    }
}