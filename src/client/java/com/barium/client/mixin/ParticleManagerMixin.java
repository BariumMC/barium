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
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayDeque;
import java.util.Map;
import java.util.Queue;

@Mixin(ParticleManager.class)
public class ParticleManagerMixin {

    @Shadow @Final private Map<ParticleTextureSheet, Queue<Particle>> particles;

    // This Mixin for the global particle limit is correct and remains unchanged.
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
     * NEW, ROBUST STRATEGY using @ModifyArg.
     * This method intercepts the queue of particles being passed to the render helper method.
     * If frustum culling is enabled, it filters this queue to only include particles
     * visible to the camera, and then passes the new, smaller queue to the original method.
     * This is much more stable than trying to cancel individual render calls.
     *
     * @param originalQueue The original queue of particles for a given texture sheet.
     * @return The original queue, or a new queue containing only visible particles.
     */
    @ModifyArg(
        method = "renderParticles(Lnet/minecraft/client/render/Camera;FLnet/minecraft/client/render/VertexConsumerProvider$Immediate;Lnet/minecraft/client/particle/ParticleTextureSheet;Ljava/util/Queue;)V",
        at = @At(
            value = "INVOKE",
            // We target the call to `vertexConsumers.getBuffer` which happens right before the loop,
            // making it a perfect and stable place to modify the arguments.
            target = "Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;getBuffer(Lnet/minecraft/client/render/RenderLayer;)Lnet/minecraft/client/render/VertexConsumer;"
        ),
        index = 4 // The 5th argument (index 4) of renderParticles is the Queue<Particle>.
    )
    private static Queue<Particle> barium$filterParticlesInFrustum(Queue<Particle> originalQueue) {
        if (BariumConfig.C.ENABLE_PARTICLE_FRUSTUM_CULLING) {
            WorldRenderer worldRenderer = MinecraftClient.getInstance().worldRenderer;
            // The Accessor is still needed to safely get the frustum object.
            Frustum frustum = ((WorldRendererAccessor) worldRenderer).getFrustum();

            if (frustum != null && !originalQueue.isEmpty()) {
                // Create a new queue containing only the visible particles from the original queue.
                Queue<Particle> visibleParticles = new ArrayDeque<>(originalQueue.size());
                for (Particle particle : originalQueue) {
                    if (frustum.isVisible(particle.getBoundingBox())) {
                        visibleParticles.add(particle);
                    }
                }
                return visibleParticles;
            }
        }

        // If the optimization is disabled or frustum is null, return the original queue unmodified.
        return originalQueue;
    }
}
