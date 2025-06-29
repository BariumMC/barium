package com.barium.client.mixin;

import com.barium.client.optimization.EntityOptimizer;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin<T extends Entity> {

    @Inject(
        method = "shouldRender(Lnet/minecraft/entity/Entity;Lnet/minecraft/client/render/Frustum;DDD)Z",
        at = @At("HEAD"),
        cancellable = true
    )
    private void barium$cullDistantEntity(T entity, Frustum frustum, double cameraX, double cameraY, double cameraZ, CallbackInfoReturnable<Boolean> cir) {
        // Renomeado para maior clareza, a chamada agora corresponde à assinatura correta.
        if (EntityOptimizer.shouldRenderEntity(entity, cameraX, cameraY, cameraZ)) {
            cir.setReturnValue(false);
        }
    }
}