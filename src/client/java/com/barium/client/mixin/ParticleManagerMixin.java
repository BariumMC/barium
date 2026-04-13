package com.barium.client.mixin;

import com.barium.client.optimization.ParticleOptimizer;
import com.barium.client.optimization.TickOptimizer;
import com.barium.config.BariumConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ParticleManager.class)
public class ParticleManagerMixin {

    // A injeção para o limite global de partículas continua correta e não precisa de alteração.
    @Inject(method = "addParticle(Lnet/minecraft/client/particle/Particle;)V", at = @At("HEAD"), cancellable = true)
    private void barium$applyGlobalParticleLimit(Particle particle, CallbackInfo ci) {
        if (BariumConfig.C.ENABLE_GLOBAL_PARTICLE_LIMIT) {
            if (ParticleOptimizer.shouldCullNewParticle()) {
                ci.cancel();
                return;
            }
            ParticleOptimizer.incrementParticleCount();
        }
    }

    @Inject(method = "tick()V", at = @At("HEAD"), cancellable = true)
    private void barium$throttleParticleManagerTick(CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (TickOptimizer.shouldSkipParticleManagerTick(client)) {
            ci.cancel();
        }
    }
}
