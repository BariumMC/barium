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
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ParticleManager.class)
public abstract class ParticleManagerMixin {

    @Shadow @Final private ClientWorld world;

    @Unique
    private Object barium$cachedParticleRenderInfo;

    /**
     * Injeta antes de Particle.buildGeometry para realizar otimização de culling.
     */
    @Inject(
        method = "renderParticles",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/particle/Particle;buildGeometry(Lnet/minecraft/client/render/VertexConsumer;Lnet/minecraft/client/render/Camera;F)V",
            shift = At.Shift.BEFORE
        ),
        cancellable = true,
        locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void barium$beforeBuildGeometry(MatrixStack matrices, VertexConsumerProvider.Immediate consumers,
                                            LightmapTextureManager lightmap, Camera camera, float tickDelta,
                                            Vec3d cameraPos, CallbackInfo ci, VertexConsumer buffer,
                                            Particle particle) {
        if (!ParticleOptimizer.shouldRenderParticle(particle, camera, this.world)) {
            ci.cancel();
        }
    }
}
