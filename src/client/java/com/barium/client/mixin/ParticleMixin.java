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
 * Mixin para a classe Particle para otimizar o ticking e renderização de partículas.
 * Baseado nos mappings Yarn 1.21.5+build.1
 */
@Mixin(Particle.class)
public abstract class ParticleMixin {

    // Shadow field para acessar o mundo da partícula
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
        if (ParticleOptimizer.shouldSkipParticleTick(self, this.world)) {
            // Cancela a execução do método tick() original
            ci.cancel();
        }
    }

    /*
     * Otimização de Renderização (shouldRenderParticle):
     * A renderização de partículas geralmente acontece em ParticleManager.renderParticles(...).
     * Precisaríamos de um mixin lá para verificar `ParticleOptimizer.shouldRenderParticle`
     * antes de adicionar a partícula ao buffer de renderização.
     *
     * Exemplo (Mixin em ParticleManager):
     * @Inject(method = "renderParticles(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;Lnet/minecraft/client/render/LightmapTextureManager;Lnet/minecraft/client/render/Camera;F)V",
     *         at = @At(value = "INVOKE", target = "Lnet/minecraft/client/particle/Particle;buildGeometry(Lnet/minecraft/client/render/VertexConsumer;Lnet/minecraft/client/render/Camera;F)V"),
     *         cancellable = true)
     * private void barium$beforeBuildGeometry(MatrixStack matrices, VertexConsumerProvider.Immediate consumers, LightmapTextureManager lightmap, Camera camera, float tickDelta, CallbackInfo ci, Particle particle) { // Precisa de LocalCapture para pegar 'particle'
     *     if (!ParticleOptimizer.shouldRenderParticle(particle, camera, this.world)) {
     *         ci.cancel(); // Cancela a chamada a buildGeometry se não deve renderizar
     *     }
     * }
     */
     
     // TODO: Implementar mixin em ParticleManager para `shouldRenderParticle` se necessário.
}
