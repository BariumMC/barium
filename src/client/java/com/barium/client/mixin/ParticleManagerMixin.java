package com.barium.client.mixin;

import com.barium.client.optimization.ParticleOptimizer;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture; // Import for local variable capture
import java.util.Iterator; // Import for Iterator if needed for loop

/**
 * Mixin para a classe ParticleManager para otimizar a renderização de partículas.
 * Baseado nos mappings Yarn 1.21.5+build.1
 */
@Mixin(ParticleManager.class)
public abstract class ParticleManagerMixin {

    @Shadow protected ClientWorld world;

    /**
     * Injects before calling particle.buildGeometry to apply render culling.
     * This prevents particles outside the camera frustum or too far away from being added
     * to the render buffer.
     *
     * Target Method Signature (Yarn 1.21.5):
     * renderParticles(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;Lnet/minecraft/client/render/LightmapTextureManager;Lnet/minecraft/client/render/Camera;F)V
     *
     * The `method` name is often obfuscated. Using intermediary name if available, or just the
     * full signature if Yarn names it.
     * For 1.21.5, the intermediary name for ParticleManager.renderParticles might be
     * `method_18118` (or similar). Let's stick to the named one if it's correct.
     *
     * We'll keep the named method and the target `buildGeometry` call. The primary
     * issue might be the LocalCapture, so let's try to be more specific.
     */
    @Inject(
        method = "renderParticles(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;Lnet/minecraft/client/render/LightmapTextureManager;Lnet/minecraft/client/render/Camera;F)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/particle/Particle;buildGeometry(Lnet/minecraft/client/render/VertexConsumer;Lnet/minecraft/client/render/Camera;F)V",
            shift = At.Shift.BEFORE // Ensure we inject right before the call
        ),
        locals = LocalCapture.CAPTURE_FAILHARD, // Tries to capture all locals
        cancellable = true
    )
    private void barium$beforeBuildGeometry(MatrixStack matrices, VertexConsumerProvider.Immediate consumers,
                                            LightmapTextureManager lightmap, Camera camera, float tickDelta,
                                            CallbackInfo ci,
                                            // Assuming the local variable 'particle' is present and is of type Particle
                                            // Mixin will try to match by type and order.
                                            // You might need to add other preceding local variables if 'particle'
                                            // is not the first one or if there are multiple of the same type.
                                            // For typical iteration loops, `particle` is often the loop variable.
                                            Iterator<Particle> iterator, // This is a common local variable if particles are iterated
                                            Particle particle // The actual particle being processed
                                            ) {
        if (particle != null && !ParticleOptimizer.shouldRenderParticle(particle, camera, this.world)) {
            ci.cancel(); // Cancel the call to buildGeometry if the particle should not be rendered
            // BariumMod.LOGGER.debug("ParticleManagerMixin: Particle culled from rendering.");
        }
    }
}