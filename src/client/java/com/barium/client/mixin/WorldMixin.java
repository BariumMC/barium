package com.barium.client.mixin;

import com.barium.config.BariumConfig;
import net.minecraft.particle.ParticleEffect;
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
     * CORREÇÃO PARA 1.21.6+: A assinatura do método 'addParticle' foi corrigida para usar 'D' para double.
     * O descritor correto para um método que recebe ParticleEffect, e depois seis doubles, é:
     * (Lnet/minecraft/particle/ParticleEffect;DDDDDD)V
     */
    @Inject(
        method = "addParticle(Lnet/minecraft/particle/ParticleEffect;DDDDDD)V", // **CORRIGIDO DESCRITOR**
        at = @At("HEAD"),
        cancellable = true
    )
    private void barium$reduceExplosionParticles(ParticleEffect parameters, double x, double y, double z, double velocityX, double velocityY, double velocityZ, CallbackInfo ci) {
        World self = (World)(Object)this;

        if (!self.isClient || !BariumConfig.C.ENABLE_EXPLOSION_PARTICLE_REDUCTION) {
            return;
        }

        // Verifique se o tipo de partícula é de explosão
        if (parameters.getType() == ParticleTypes.EXPLOSION || parameters.getType() == ParticleTypes.EXPLOSION_EMITTER) {
            if (ThreadLocalRandom.current().nextInt(4) != 0) {
                ci.cancel();
            }
        }
    }
}