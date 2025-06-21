// --- Substitua o conteúdo em: src/client/java/com/barium/client/mixin/ParticleMixin.java ---
package com.barium.client.mixin;

import com.barium.client.optimization.ParticleOptimizer;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.render.Camera;
import net.minecraft.client.MinecraftClient;
// A importação do VertexConsumer não é mais necessária aqui
// import net.minecraft.client.render.VertexConsumer; 

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Particle.class)
public abstract class ParticleMixin {

    // Otimização para pular o tick (lógica) de partículas distantes.
    // Esta parte não conflita e é uma boa otimização de CPU.
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void barium$skipDistantTick(CallbackInfo ci) {
        Particle self = (Particle)(Object)this;
        // A câmera pode não estar pronta na inicialização, então adicionamos uma verificação
        if (MinecraftClient.getInstance().gameRenderer == null) {
            return;
        }
        Camera camera = MinecraftClient.getInstance().gameRenderer.getCamera();

        if (ParticleOptimizer.shouldSkipParticleTick(self, camera)) {
            ci.cancel();
        }
    }
}