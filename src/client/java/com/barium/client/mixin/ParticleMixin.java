package com.barium.client.mixin;

import com.barium.client.optimization.ParticleOptimizer;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.render.Camera;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumer; // Importar VertexConsumer
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Particle.class)
public abstract class ParticleMixin {

    // Otimização para pular o tick (atualização) de partículas distantes
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void barium$skipDistantTick(CallbackInfo ci) {
        Particle self = (Particle)(Object)this;
        Camera camera = MinecraftClient.getInstance().gameRenderer.getCamera();

        if (ParticleOptimizer.shouldSkipParticleTick(self, camera)) {
            ci.cancel();
        }
    }

    // Otimização para pular a renderização (construção da geometria) de partículas distantes
    @Inject(method = "buildGeometry", at = @At("HEAD"), cancellable = true)
    private void barium$skipDistantRender(VertexConsumer vertexConsumer, Camera camera, float tickDelta, CallbackInfo ci) {
        Particle self = (Particle)(Object)this;
        // Usa a mesma lógica de shouldRenderParticle do ParticleOptimizer
        if (!ParticleOptimizer.shouldRenderParticle(self, camera)) {
            ci.cancel(); // Cancela a execução do método buildGeometry
        }
    }
}