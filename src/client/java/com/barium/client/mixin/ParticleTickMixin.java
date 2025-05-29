package com.barium.client.mixin;

import com.barium.client.optimization.ParticleOptimizer;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin para a classe Particle para otimizar o ticking de partículas.
 * Baseado nos mappings Yarn 1.21.5+build.1
 */
@Mixin(Particle.class)
public abstract class ParticleTickMixin { // Renomeado para especificar o propósito

    // Shadow field para acessar o mundo da partícula
    // A sintaxe correta para @Shadow não inclui 'targets' para campos simples quando o mixin é direto na classe.
    @Shadow protected ClientWorld world;

    /**
     * Injeta no início do método tick() da partícula.
     * Verifica se o tick da partícula deve ser pulado com base na distância (LOD).
     *
     * Target Method Signature (Yarn 1.21.5): Lnet/minecraft/client/particle/Particle;tick()V
     */
    @Inject(
        method = "tick()V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void barium$onTick(CallbackInfo ci) {
        // Converte 'this' para a instância de Particle
        Particle self = (Particle)(Object)this;

        // Verifica se o tick deve ser pulado pelo otimizador
        // 'this.world' é válido aqui porque o mixin está na classe Particle
        if (ParticleOptimizer.shouldSkipParticleTick(self, this.world)) {
            // Cancela a execução do método tick() original
            ci.cancel();
        }
    }
}