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
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

// Mixin for ParticleManager to apply rendering optimizations (culling).
@Mixin(ParticleManager.class)
public abstract class ParticleManagerMixin {

    // Shadow the clientWorld field from ParticleManager
    @Shadow @Final private ClientWorld world;

    /**
     * Injects before the call to Particle.buildGeometry to check if a particle should be rendered.
     * If ParticleOptimizer indicates that the particle should not be rendered, the method call is cancelled.
     * This effectively prevents the particle from being added to the rendering buffer.
     *
     * Target Method Signature (Yarn 1.21.5):
     * renderParticles(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;Lnet/minecraft/client/render/LightmapTextureManager;Lnet/minecraft/client/render/Camera;F)V
     *
     * The `at` target `INVOKE` with `shift = At.Shift.BEFORE` before `buildGeometry`
     * and `local` to capture the 'particle' variable is crucial.
     */
    @Inject(
        method = "renderParticles(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;Lnet/minecraft/client/render/LightmapTextureManager;Lnet/minecraft/client/render/Camera;F)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/particle/Particle;buildGeometry(Lnet/minecraft/client/render/VertexConsumer;Lnet/minecraft/client/render/Camera;F)V",
            shift = At.Shift.BEFORE
        ),
        cancellable = true,
        locals = LocalCapture.CAPTURE_FAILHARD // Capture local variables required for `particle`
    )
    private void barium$beforeBuildGeometry(MatrixStack matrices, VertexConsumerProvider.Immediate consumers,
                                            LightmapTextureManager lightmap, Camera camera, float tickDelta,
                                            CallbackInfo ci, VertexConsumer buffer, // These are captured locals before Particle loop starts, irrelevant for 'particle'
                                            Particle particle // This local variable `particle` is the one we want to check
    ) {
        if (!ParticleOptimizer.shouldRenderParticle(particle, camera, this.world)) {
            // Cancel the injection, preventing the buildGeometry call for this particle.
            ci.cancel();
        }
    }
}