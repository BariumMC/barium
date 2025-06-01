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

/**
 * Mixin para a classe ParticleManager para otimizar a renderização de partículas.
 * Baseado nos mappings Yarn 1.21.5+build.1
 */
@Mixin(ParticleManager.class)
public abstract class ParticleManagerMixin {

    @Shadow protected ClientWorld world;

    /**
     * Injeta antes de chamar particle.buildGeometry para aplicar o culling de renderização.
     * Isso impede que partículas fora do frustum da câmera ou muito distantes sejam adicionadas
     * ao buffer de renderização.
     *
     * Target Method Signature (Yarn 1.21.5):
     * renderParticles(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;Lnet/minecraft/client/render/LightmapTextureManager;Lnet/minecraft/client/render/Camera;F)V
     *
     * Injetar no INVOKE de buildGeometry nos dará acesso à instância da partícula localmente.
     */
    @Inject(
        method = "renderParticles(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;Lnet/minecraft/client/render/LightmapTextureManager;Lnet/minecraft/client/render/Camera;F)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/particle/Particle;buildGeometry(Lnet/minecraft/client/render/VertexConsumer;Lnet/minecraft/client/render/Camera;F)V"
            // shift = At.Shift.BEFORE // Optional: shift to before the instruction if needed
        ),
        locals = LocalCapture.CAPTURE_FAILHARD, // Captura variáveis locais, incluindo a partícula iterada
        cancellable = true
    )
    private void barium$beforeBuildGeometry(MatrixStack matrices, VertexConsumerProvider.Immediate consumers,
                                            LightmapTextureManager lightmap, Camera camera, float tickDelta,
                                            CallbackInfo ci, // Original callback info
                                            // Captured locals (order and type must match the target method's locals)
                                            // You might need to check the exact bytecode to get these right
                                            // The important one is the 'Particle' instance.
                                            // This example assumes 'particle' is available as the last local before the target INVOKE.
                                            // If it fails, use a decompiler on the target method to get the correct locals.
                                            Particle particle // Captures the particle being processed
                                            ) {
        if (!ParticleOptimizer.shouldRenderParticle(particle, camera, this.world)) {
            ci.cancel(); // Cancela a chamada a buildGeometry se não deve renderizar
            // BariumMod.LOGGER.debug("ParticleManagerMixin: Particle culled from rendering.");
        }
    }
}