package com.barium.client.mixin;

import com.barium.client.optimization.ParticleOptimizer;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.render.Camera;
import net.minecraft.client.MinecraftClient;
// import net.minecraft.client.render.VertexConsumer; // Não é mais necessário

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

    // A injeção para 'buildGeometry' foi removida temporariamente
    // devido a problemas de compilação com a assinatura do método.
    // A otimização de renderização de partículas estará desativada por enquanto.
}
