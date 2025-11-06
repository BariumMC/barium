package com.barium.client.mixin;

import com.barium.config.BariumConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayDeque;
import java.util.Map;
import java.util.Queue;
import java.util.stream.Collectors;

@Mixin(ParticleManager.class)
public class ParticleManagerMixin {

    @Shadow @Final private Map<ParticleTextureSheet, Queue<Particle>> particles;

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

    /**
     * CORREÇÃO 25w45a:
     * O método `renderParticles` foi removido. A lógica de renderização agora está no método `render`.
     * Usamos um @Redirect para interceptar a `Queue` de partículas que o loop `for` usa.
     * Nós filtramos essa queue com nosso frustum culling e retornamos a nova queue filtrada.
     */
    @Redirect(
        method = "render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;Lnet/minecraft/client/render/LightmapTextureManager;Lnet/minecraft/client/render/Camera;F)V",
        at = @At(value = "INVOKE", target = "Ljava/util/Map;get(Ljava/lang/Object;)Ljava/lang/Object;")
    )
    private Object barium$filterParticlesInFrustum(Map<ParticleTextureSheet, Queue<Particle>> particleMap, Object sheet) {
        Queue<Particle> originalQueue = particleMap.get(sheet);
        if (originalQueue == null || originalQueue.isEmpty() || !BariumConfig.C.ENABLE_PARTICLE_FRUSTUM_CULLING) {
            return originalQueue;
        }

        Frustum frustum = WorldRendererMixin.capturedFrustum;
        if (frustum == null) {
            return originalQueue;
        }

        // Filtra a queue para incluir apenas partículas visíveis
        Queue<Particle> visibleParticles = new ArrayDeque<>(originalQueue.size());
        for (Particle particle : originalQueue) {
            if (frustum.isVisible(particle.getBoundingBox())) {
                visibleParticles.add(particle);
            }
        }
        return visibleParticles;
    }
}