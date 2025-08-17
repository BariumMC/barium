package com.barium.client.mixin;

import com.barium.config.BariumConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Iterator;
import java.util.Map;
import java.util.Queue;

@Mixin(ParticleManager.class)
public class ParticleManagerMixin {

    @Shadow @Final private Map<ParticleTextureSheet, Queue<Particle>> particles;

    // Optimization: Global Particle Limit
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

    // Optimization: Particle Frustum Culling (using a stable Local Capture Inject)
    @Inject(
        // This is the correct signature for the internal helper method provided by your mappings.
        method = "renderParticles(Lnet/minecraft/client/render/Camera;FLnet/minecraft/client/render/VertexConsumerProvider$Immediate;Lnet/minecraft/client/particle/ParticleTextureSheet;Ljava/util/Queue;)V",
        at = @At(
            value = "INVOKE",
            // The target remains the same: the method call that builds the particle's geometry for rendering.
            target = "Lnet/minecraft/client/particle/Particle;buildGeometry(Lnet/minecraft/client/render/VertexConsumer;Lnet/minecraft/client/render/Camera;FFFFFF)V"
        ),
        cancellable = true,
        locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void barium$cullParticlesInFrustum(Camera camera, float tickDelta, VertexConsumerProvider.Immediate vertexConsumers, ParticleTextureSheet sheet, Queue<Particle> particles, CallbackInfo ci, Iterator var7, Particle particle) {
        if (BariumConfig.C.ENABLE_PARTICLE_FRUSTUM_CULLING) {
            WorldRenderer worldRenderer = MinecraftClient.getInstance().worldRenderer;
            // The Accessor allows us to safely get the frustum object
            Frustum frustum = ((WorldRendererAccessor) worldRenderer).getFrustum();

            if (frustum != null && !frustum.isVisible(particle.getBoundingBox())) {
                ci.cancel(); // Skips rendering the particle if it's outside the camera's view
            }
        }
    }
}