package com.barium.client.mixin;

import com.barium.client.optimization.ParticleOptimizer;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Particle.class)
public abstract class ParticleMixin {

    // Shadow para acessar o mundo da partícula
    @Shadow protected ClientWorld world;

    /**
     * Injeta no início do método tick() da partícula.
     * Pula o tick se estiver muito longe da câmera.
     */
    @Inject(method = "tick()V", at = @At("HEAD"), cancellable = true)
    private void barium$onTick(CallbackInfo ci) {
        Particle self = (Particle)(Object)this;

        Camera camera = MinecraftClient.getInstance().gameRenderer.getCamera();

        if (ParticleOptimizer.shouldSkipParticleTick(self, camera)) {
            ci.cancel();
        }
    }
}
