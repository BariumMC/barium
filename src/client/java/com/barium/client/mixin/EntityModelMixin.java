package com.barium.client.mixin;

import com.barium.client.optimization.EntityOptimizer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityModel.class)
public class EntityModelMixin<T extends Entity> {

    @Inject(
        // CORREÇÃO: Removemos a assinatura explícita do método para maior robustez.
        method = "setAngles",
        at = @At("HEAD"),
        cancellable = true
    )
    private void barium$cullDistantAnimation(T entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch, CallbackInfo ci) {
        if (!EntityOptimizer.shouldAnimateEntity(entity)) {
            ci.cancel();
        }
    }
}