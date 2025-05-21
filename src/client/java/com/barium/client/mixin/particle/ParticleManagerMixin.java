package com.barium.client.mixin.particle;

import com.barium.client.optimization.ParticleOptimizer;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ParticleManager.class)
public abstract class ParticleManagerMixin {

    // Redireciona a chamada ao método tick() de cada partícula dentro do ParticleManager.tick()
    // Isso nos permite decidir se a partícula deve ser "ticked" ou não.
    @Redirect(
        method = "tick",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/particle/Particle;tick()V"
        )
    )
    private void barium$redirectParticleTick(Particle particle) {
        // Se ParticleOptimizer.shouldTickParticle retornar true, chama o tick original.
        // Caso contrário, a chamada ao tick original é omitida, otimizando o desempenho.
        if (ParticleOptimizer.shouldTickParticle(particle)) {
            particle.tick();
        }
    }
}