package com.barium.client.mixin;

import com.barium.config.BariumConfig;
import net.minecraft.world.explosion.Explosion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.concurrent.ThreadLocalRandom;

@Mixin(Explosion.class)
public class ExplosionMixin {

    // Intercepta a criação de partículas de explosão e fumaça.
    @ModifyArg(
        method = "affectWorld(Z)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;spawnParticles(Lnet/minecraft/particle/ParticleEffect;DDDIDD)I"),
        index = 0 // Modifica o primeiro argumento (ParticleEffect)
    )
    private <T extends net.minecraft.particle.ParticleEffect> T barium$reduceExplosionParticles(T particle) {
        if (BariumConfig.C.ENABLE_EXPLOSION_PARTICLE_REDUCTION) {
            // Tem 75% de chance de pular a criação da partícula.
            if (ThreadLocalRandom.current().nextInt(4) != 0) {
                return null; // Retornar null impede que a partícula seja criada.
            }
        }
        return particle;
    }
}