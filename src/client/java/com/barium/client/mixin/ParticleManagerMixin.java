package com.barium.client.mixin;

import com.barium.config.BariumConfig;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.Camera;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.Queue;
import java.util.Iterator;

@Mixin(ParticleManager.class)
public class ParticleManagerMixin {

    @Shadow @Final private Map<ParticleTextureSheet, Queue<Particle>> particles;

    // Otimização de Limite Global de Partículas
    @Inject(method = "addParticle(Lnet/minecraft/client/particle/Particle;)V", at = @At("HEAD"), cancellable = true)
    private void barium$applyGlobalParticleLimit(Particle particle, CallbackInfo ci) {
        if (!BariumConfig.C.ENABLE_GLOBAL_PARTICLE_LIMIT) {
            return;
        }

        int currentCount = 0;
        for (Queue<Particle> queue : this.particles.values()) {
            currentCount += queue.size();
        }

        if (currentCount >= BariumConfig.C.MAX_GLOBAL_PARTICLES) {
            ci.cancel();
        }
    }

    // Otimização de Frustum Culling para Partículas
    @Redirect(
        method = "renderParticles(Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;Lnet/minecraft/client/render/LightmapTextureManager;Lnet/minecraft/client/render/Camera;FLnet/minecraft/client/render/Frustum;)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/particle/Particle;buildGeometry(Lnet/minecraft/client/render/VertexConsumer;Lnet/minecraft/client/render/Camera;F)V")
    )
    private void barium$cullParticlesInFrustum(Particle particle, VertexConsumer vertexConsumer, Camera camera, float tickDelta, VertexConsumerProvider.Immediate immediate, LightmapTextureManager lightmapTextureManager, Camera cameraArg, float tickDeltaArg, Frustum frustum) {
        if (BariumConfig.C.ENABLE_PARTICLE_FRUSTUM_CULLING) {
            if (frustum != null && !frustum.isVisible(particle.getBoundingBox())) {
                return; // Não renderiza a partícula se estiver fora da visão
            }
        }
        // Se a otimização estiver desligada ou a partícula for visível, chama o método original.
        particle.buildGeometry(vertexConsumer, camera, tickDelta);
    }
}