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

    // Corrected method name from "expire" to "markDead" for 1.21.5 mappings
    @Inject(method = "markDead", at = @At("HEAD"))
    private void barium$onMarkDead(CallbackInfo ci) {
        // When a particle is marked dead, remove it from the optimizer's caches
        ParticleOptimizer.removeParticle((Particle)(Object)this);
    }
}