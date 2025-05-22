package com.barium.client.mixin.particle;

import com.barium.client.optimization.ParticleOptimizer;
import com.barium.client.optimization.ParticleTracker;
import net.minecraft.client.particle.Particle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Particle.class)
public abstract class ParticleMixin {

    // Adicionado o descritor explícito "()V" para o método expire
    @Inject(method = "expire()V", at = @At("HEAD"))
    private void barium$onExpireHead(CallbackInfo ci) {
        ParticleTracker.decrementParticleCount();
        ParticleOptimizer.removeParticle((Particle)(Object)this);
    }
}