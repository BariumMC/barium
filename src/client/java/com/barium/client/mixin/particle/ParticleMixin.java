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

    @Inject(method = "expire", at = @At("HEAD"))
    private void barium$onExpireHead(CallbackInfo ci) {
        ParticleTracker.decrementParticleCount();
        ParticleOptimizer.removeParticle((Particle)(Object)this);
    }

    // REMOVIDA: A injeção para buildGeometry(VertexConsumer) foi removida desta classe.
    // A otimização de renderização será feita no ParticleManagerMixin via manipulação do iterador.
}