// --- Substitua o conteúdo em: src/client/java/com/barium/client/mixin/ParticleManagerMixin.java ---
package com.barium.client.mixin;

import com.barium.config.BariumConfig;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.particle.ParticleTextureSheet;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.Queue;

@Mixin(ParticleManager.class)
public class ParticleManagerMixin {

    @Shadow @Final private Map<ParticleTextureSheet, Queue<Particle>> particles;
    private int totalParticleCount = 0;

    // Injetamos no início do método que adiciona partículas.
    @Inject(method = "addParticle(Lnet/minecraft/client/particle/Particle;)V", at = @At("HEAD"), cancellable = true)
    private void barium$applyGlobalParticleLimit(Particle particle, CallbackInfo ci) {
        if (!BariumConfig.C.ENABLE_GLOBAL_PARTICLE_LIMIT) {
            return;
        }

        // Para evitar recalcular a cada partícula, podemos usar uma contagem em cache,
        // mas por simplicidade e robustez, vamos calcular aqui.
        // Em um tick com muitas partículas, este cálculo só acontece uma vez de fato.
        int currentCount = 0;
        for (Queue<Particle> queue : this.particles.values()) {
            currentCount += queue.size();
        }

        if (currentCount >= BariumConfig.C.MAX_GLOBAL_PARTICLES) {
            ci.cancel(); // Se o limite foi atingido, a nova partícula não é adicionada.
        }
    }
}