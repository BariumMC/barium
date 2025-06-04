package com.barium.client.mixin;

import com.barium.client.optimization.ParticleOptimizer;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ParticleManager.class)
public class ParticleManagerMixin {

    /**
     * Redireciona a chamada para buildGeometry.
     * Só renderiza partículas próximas.
     */
    @Redirect(
        method = "renderParticles(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;Lnet/minecraft/client/render/LightmapTextureManager;Lnet/minecraft/client/render/Camera;F)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/particle/Particle;buildGeometry(Lnet/minecraft/client/render/VertexConsumer;Lnet/minecraft/client/render/Camera;F)V"
        )
    )
    private void barium$shouldRenderParticle(Particle particle, VertexConsumer vertexConsumer, Camera camera, float tickDelta) {
        if (ParticleOptimizer.shouldRenderParticle(particle, camera)) {
            particle.buildGeometry(vertexConsumer, camera, tickDelta);
        }
        // Se não deve renderizar: ignora silenciosamente.
    }
}
