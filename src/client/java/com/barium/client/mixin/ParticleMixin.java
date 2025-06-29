package com.barium.client.mixin;

import com.barium.client.optimization.ParticleOptimizer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.render.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Particle.class)
public abstract class ParticleMixin {

    /**
     * Otimização para pular o tick (lógica) de partículas muito distantes.
     */
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void barium$skipDistantTick(CallbackInfo ci) {
        if (!BariumConfig.C.ENABLE_PARTICLE_OPTIMIZATION) return;

        Camera camera = MinecraftClient.getInstance().gameRenderer.getCamera();
        Particle self = (Particle)(Object)this;

        // CORREÇÃO: A chamada agora corresponde à assinatura correta em ParticleOptimizer.
        if (ParticleOptimizer.shouldSkipParticleTick(self, camera.getPos())) {
            ci.cancel();
        }
    }
}