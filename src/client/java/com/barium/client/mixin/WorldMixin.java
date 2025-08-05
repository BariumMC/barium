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
     * Injeta no início do método que adiciona QUALQUER partícula ao mundo.
     * CORREÇÃO: Voltamos para a assinatura correta de 1.21.x que inclui o booleano.
     */
    @Inject(
        method = "addParticle(Lnet/minecraft/particle/ParticleEffect;ZDDDDDD)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void barium$reduceExplosionParticles(ParticleEffect parameters, boolean ignoreRange, double x, double y, double z, double velocityX, double velocityY, double velocityZ, CallbackInfo ci) {
        World self = (World)(Object)this;

        // A otimização só deve rodar no lado do cliente.
        if (!self.isClient) {
            return;
        }

        if (!BariumConfig.C.ENABLE_EXPLOSION_PARTICLE_REDUCTION) {
            return;
        }

        // Verifica se a partícula é de uma explosão.
        if (parameters.getType() == ParticleTypes.EXPLOSION || parameters.getType() == ParticleTypes.EXPLOSION_EMITTER) {
            // Tem 75% de chance de pular a criação da partícula.
            if (ThreadLocalRandom.current().nextInt(4) != 0) {
                ci.cancel(); // Cancela a adição desta partícula.
            }
        }
    }
}