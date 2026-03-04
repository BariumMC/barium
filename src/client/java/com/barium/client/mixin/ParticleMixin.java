package com.barium.client.mixin;

import com.barium.client.optimization.ParticleOptimizer;
// CORREÇÃO: Import ausente adicionado.
import com.barium.config.BariumConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.render.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Particle.class)
public abstract class ParticleMixin {

    @Inject(method = "tick()V", at = @At("HEAD"), cancellable = true)
    private void barium$onParticleTick(CallbackInfo ci) {
        if (!BariumConfig.C.ENABLE_PARTICLE_OPTIMIZATION) return;

        Camera camera = MinecraftClient.getInstance().gameRenderer.getCamera();
        Particle self = (Particle)(Object)this;

        if (ParticleOptimizer.shouldSkipParticleTick(self, camera.getPos())) {
            ci.cancel();
        }
    }

    @Inject(method = "markDead", at = @At("HEAD"))
    private void barium$onParticleDead(CallbackInfo ci) {
        if (!BariumConfig.C.ENABLE_GLOBAL_PARTICLE_LIMIT) return;
        ParticleOptimizer.decrementParticleCount();
    }
}
