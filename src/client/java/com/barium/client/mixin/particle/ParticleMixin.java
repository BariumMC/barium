package com.barium.client.mixin.particle;

import com.barium.config.BariumConfig;
import com.barium.client.optimization.ParticleOptimizer;
import net.minecraft.client.particle.Particle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Particle.class)
public abstract class ParticleMixin implements ParticleAccessor {

    // REMOVIDO: A injeção no método render() foi movida para ParticleManagerMixin.

    // Injeta no início do método tick() da partícula para controlar sua atualização
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void barium$beforeTick(CallbackInfo ci) {
        // Se ParticleOptimizer.shouldTickParticle retornar false, cancela o método tick()
        // Isso impede que a lógica de atualização da partícula seja executada para este tick.
        if (!ParticleOptimizer.shouldTickParticle((Particle)(Object)this)) {
            ci.cancel(); 
        }
    }

    // Injeta no início do método markDead() da partícula
    @Inject(method = "markDead", at = @At("HEAD"))
    private void barium$onMarkDead(CallbackInfo ci) {
        // Quando uma partícula é marcada como morta, remova-a dos nossos mapas de otimização
        ParticleOptimizer.removeParticle((Particle) (Object) this);
    }
}