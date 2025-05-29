package com.barium.client.mixin;

import com.barium.client.optimization.ParticleOptimizer;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager; // Import correto: net.minecraft.client.particle
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.VertexConsumer; // Usado na assinatura do buildGeometry
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld; // Para 'particle.world'

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

/**
 * Mixin para a classe ParticleManager para otimizar a renderização de partículas.
 * Baseado nos mappings Yarn 1.21.5+build.1
 */
@Mixin(ParticleManager.class) // Mixa apenas em ParticleManager
public abstract class ParticleManagerRenderMixin { // Renomeado para especificar o propósito

    /**
     * Injeta no método renderParticles de ParticleManager para otimizar a renderização.
     * Verifica se a partícula deve ser renderizada antes de adicionar ao buffer.
     *
     * Target Class: net.minecraft.client.particle.ParticleManager
     * Target Method Signature (Yarn 1.21.5): renderParticles(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;Lnet/minecraft/client/render/LightmapTextureManager;Lnet/minecraft/client/render/Camera;F)V
     *
     * A local `particle` é capturada do loop interno.
     */
    @Inject(
        method = "renderParticles(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;Lnet/minecraft/client/render/LightmapTextureManager;Lnet/minecraft/client/render/Camera;F)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/particle/Particle;buildGeometry(Lnet/minecraft/client/render/VertexConsumer;Lnet/minecraft/client/render/Camera;F)V",
            shift = At.Shift.BEFORE // Injeta logo antes da partícula construir sua geometria
        ),
        locals = LocalCapture.CAPTURE_FAILSOFT, // Tenta capturar variáveis locais
        cancellable = true
    )
    private void barium$beforeBuildGeometry(MatrixStack matrices, VertexConsumerProvider.Immediate consumers, LightmapTextureManager lightmap, Camera camera, float tickDelta, CallbackInfo ci, /* Captured Locals start here: */ Particle particle) {
        // 'particle' é capturada como uma variável local do loop do ParticleManager.
        // A própria instância de Particle (`particle`) contém seu mundo (`particle.world`), que pode ser acessado.
        if (!ParticleOptimizer.shouldRenderParticle(particle, camera, particle.setWorld)) { // Use particle.world diretamente
            ci.cancel(); // Cancela a chamada a buildGeometry para esta partícula
        }
    }
}