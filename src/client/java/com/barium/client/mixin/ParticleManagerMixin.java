package com.barium.client.mixin;

import com.barium.client.optimization.ParticleOptimizer;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.render.Camera;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(ParticleManager.class)
public class ParticleManagerMixin {

    @ModifyArg(
        method = "renderParticles(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;Lnet/minecraft/client/render/LightmapTextureManager;Lnet/minecraft/client/render/Camera;F)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/particle/Particle;buildGeometry(Lnet/minecraft/client/render/VertexConsumer;Lnet/minecraft/client/render/Camera;F)V"
        ),
        index = 0
    )
    private Particle barium$skipDistantRender(Particle particle) {
        Camera camera = MinecraftClient.getInstance().gameRenderer.getCamera();
        if (!ParticleOptimizer.shouldRenderParticle(particle, camera)) {
            return null;  // ou: um NoOpParticle se necessário
        }
        return particle;
    }
}
