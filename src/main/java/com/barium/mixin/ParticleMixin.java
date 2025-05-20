package com.barium.mixin;

import com.barium.optimization.ParticleOptimizer;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.render.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Particle.class)
public class ParticleMixin {
    
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void onTick(CallbackInfo ci) {
        Particle particle = (Particle)(Object)this;
        
        // Verifica se a partícula deve ser atualizada neste tick
        if (!ParticleOptimizer.shouldTickParticle(particle)) {
            ci.cancel(); // Cancela o tick desta partícula
        }
    }
    
    @Inject(method = "shouldRender", at = @At("HEAD"), cancellable = true)
    private void onShouldRender(Camera camera, CallbackInfoReturnable<Boolean> cir) {
        Particle particle = (Particle)(Object)this;
        
        // Verifica se a partícula deve ser renderizada
        if (!ParticleOptimizer.shouldRenderParticle(particle, camera)) {
            cir.setReturnValue(false); // Não renderiza esta partícula
        }
    }
}
