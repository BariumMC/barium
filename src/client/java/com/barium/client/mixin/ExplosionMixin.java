package com.barium.client.mixin;

import com.barium.config.BariumConfig;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.concurrent.ThreadLocalRandom;

@Mixin(Explosion.class)
public class ExplosionMixin {

    @Shadow @Final private World world;

    /**
     * Redirecionamos a chamada que gera as partículas da explosão para nosso próprio método.
     * Isso nos permite decidir se a partícula deve ou não ser criada.
     * Esta abordagem é mais robusta que @ModifyArg.
     */
    @Redirect(
        method = "affectWorld(Z)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/World;addParticle(Lnet/minecraft/particle/ParticleEffect;DDDDDD)V"
        )
    )
    private void barium$reduceExplosionParticles(World world, net.minecraft.particle.ParticleEffect parameters, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
        if (!this.world.isClient || !BariumConfig.C.ENABLE_EXPLOSION_PARTICLE_REDUCTION) {
            // Se a otimização estiver desativada, chama o método original.
            this.world.addParticle(parameters, x, y, z, velocityX, velocityY, velocityZ);
            return;
        }
        
        // Tem 75% de chance de pular a criação da partícula.
        if (ThreadLocalRandom.current().nextInt(4) == 0) {
            this.world.addParticle(parameters, x, y, z, velocityX, velocityY, velocityZ);
        }
    }
}