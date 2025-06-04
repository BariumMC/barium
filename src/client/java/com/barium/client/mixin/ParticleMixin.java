package com.barium.client.mixin;

import com.barium.client.optimization.ParticleOptimizer;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.render.Camera;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Particle.class)
public abstract class ParticleMixin {

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void barium$skipDistantTick(CallbackInfo ci) {
        Particle self = (Particle)(Object)this;
        Camera camera = MinecraftClient.getInstance().gameRenderer.getCamera();

        if (ParticleOptimizer.shouldSkipParticleTick(self, camera)) {
            ci.cancel();
        }
    }
}
