package com.barium.client.mixin;

import com.barium.client.optimization.ParticleOptimizer;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.render.Camera;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumer; // Importação ainda necessária

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Particle.class)
public abstract class ParticleMixin {

    // Otimização para pular o tick (lógica) de partículas distantes.
    // Esta parte já estava correta.
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void barium$skipDistantTick(CallbackInfo ci) {
        Particle self = (Particle)(Object)this;
        Camera camera = MinecraftClient.getInstance().gameRenderer.getCamera();

        if (ParticleOptimizer.shouldSkipParticleTick(self, camera)) {
            ci.cancel();
        }
    }

    // VERSÃO FINAL CORRIGIDA: Injeção no método 'render', que substituiu 'buildGeometry'.
    // O alvo da injeção agora é o método que você encontrou na documentação.
    @Inject(
        method = "render",
        at = @At("HEAD"),
        cancellable = true
    )
    private void barium$cancelDistantParticleRender(VertexConsumer vertexConsumer, Camera camera, float tickProgress, CallbackInfo ci) {
        Particle self = (Particle)(Object)this;
        // A lógica de otimização continua a mesma, apenas o ponto de injeção mudou.
        if (!ParticleOptimizer.shouldRenderParticle(self, camera)) {
            ci.cancel(); // Cancela a renderização se a partícula estiver muito longe.
        }
    }
}
