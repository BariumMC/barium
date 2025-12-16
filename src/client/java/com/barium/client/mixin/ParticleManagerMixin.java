package com.barium.client.mixin;

import com.barium.config.BariumConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.SubmittableBatch;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Queue;
import java.util.stream.Collectors;

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

        // Reduz particles de explosão
        if (BariumConfig.C.ENABLE_EXPLOSION_PARTICLE_REDUCTION) {
            if (isExplosionParticle(particle)) {
                // Cancela 50% das particles de explosão
                if ((particle.hashCode() % 2) == 0) {
                    ci.cancel();
                }
            }
        }

    private boolean isExplosionParticle(Particle particle) {
        // Verifica se a particle é de explosão baseada no tipo
        String className = particle.getClass().getSimpleName();
        return className.contains("Explosion") || className.contains("Smoke") || className.contains("LargeExplosion");
    }
    /**
     * CORREÇÃO: O método `renderParticles` foi removido. A nova renderização de partículas
     * acontece em `addToBatch`. Esta injeção intercepta a chamada no início.
     * Se o frustum culling de partículas estiver ativado, a lógica (agora em uma classe
     * de otimização) irá filtrar as partículas antes que o Minecraft tente renderizá-las.
     * Esta abordagem é mais complexa, mas é a forma correta de fazer na nova API.
     *
     * Para simplificar, a lógica de culling será movida para o método tick da própria partícula.
     * Esta injeção está sendo removida para evitar complexidade e crashes.
     * A otimização de frustum de partículas foi movida para ParticleMixin.
     */

    // O @ModifyArg foi removido pois o método alvo não existe mais.
    // A otimização de frustum culling para partículas foi integrada de forma mais robusta
    // em `ParticleMixin` e `WorldRendererMixin`, onde o frustum é acessado e verificado
    // de forma segura a cada tick.
}