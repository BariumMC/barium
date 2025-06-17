package com.barium.client.mixin;

import com.barium.config.BariumConfig;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.ThreadLocalRandom;

@Mixin(World.class)
public abstract class WorldMixin {

    /**
     * Injeta no início do método que adiciona QUALQUER partícula ao mundo.
     * CORREÇÃO: A assinatura foi atualizada para a versão 1.21.5+, que inclui um booleano 'force'.
     */
    @Inject(
        method = "addParticle(Lnet/minecraft/particle/ParticleEffect;ZDDDDDD)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void barium$reduceExplosionParticles(net.minecraft.particle.ParticleEffect parameters, boolean force, double x, double y, double z, double velocityX, double velocityY, double velocityZ, CallbackInfo ci) {
        World self = (World)(Object)this;

        if (!self.isClient || !BariumConfig.C.ENABLE_EXPLOSION_PARTICLE_REDUCTION) {
            return;
        }

        if (parameters.getType() == ParticleTypes.EXPLOSION || parameters.getType() == ParticleTypes.EXPLOSION_EMITTER) {
            if (ThreadLocalRandom.current().nextInt(4) != 0) {
                ci.cancel();
            }
        }
    }
}