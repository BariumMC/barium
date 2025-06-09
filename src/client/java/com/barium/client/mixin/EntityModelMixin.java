package com.barium.client.mixin;

import com.barium.client.optimization.EntityOptimizer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin para a classe genérica EntityModel.
 * Intercepta o método setAngles para evitar cálculos de animação em entidades distantes.
 * Isto é mais robusto do que mirar em LivingEntityModel, pois abrange todos os modelos de entidade.
 */
@Mixin(EntityModel.class)
public class EntityModelMixin<T extends Entity> {

    @Inject(
        // A assinatura do método agora usa a classe base 'Entity'
        method = "setAngles(Lnet/minecraft/entity/Entity;FFFFF)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void barium$cullDistantAnimation(T entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch, CallbackInfo ci) {
        // A lógica do otimizador permanece a mesma, pois já funciona com a classe base 'Entity'
        if (!EntityOptimizer.shouldAnimateEntity(entity)) {
            ci.cancel(); // Cancela o cálculo dos ângulos da animação
        }
    }
}