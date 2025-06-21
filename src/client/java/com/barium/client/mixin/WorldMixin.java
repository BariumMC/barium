// --- Substitua o conteúdo em: src/client/java/com/barium/client/mixin/WorldMixin.java ---
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
     * Esta é a abordagem mais robusta para otimizar partículas de explosão.
     */
    @Inject(
        method = "addParticle(Lnet/minecraft/particle/ParticleEffect;DDDDDD)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void barium$reduceExplosionParticles(net.minecraft.particle.ParticleEffect parameters, double x, double y, double z, double velocityX, double velocityY, double velocityZ, CallbackInfo ci) {
        World self = (World)(Object)this;

        // A otimização só deve rodar no cliente.
        if (!self.isClient) {
            return;
        }

        // Se a otimização estiver desligada, não fazemos nada.
        if (!BariumConfig.C.ENABLE_EXPLOSION_PARTICLE_REDUCTION) {
            return;
        }

        // Verificamos se a partícula que está sendo adicionada é uma das partículas de explosão.
        if (parameters.getType() == ParticleTypes.EXPLOSION || parameters.getType() == ParticleTypes.EXPLOSION_EMITTER) {
            // Tem 75% de chance de pular a criação da partícula.
            if (ThreadLocalRandom.current().nextInt(4) != 0) {
                ci.cancel(); // Cancela a adição desta partícula.
            }
        }
    }
}