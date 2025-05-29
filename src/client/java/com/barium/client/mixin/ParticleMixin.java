package com.barium.client.mixin;

import com.barium.client.optimization.ParticleOptimizer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.ParticleManager; // Adicionado
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

/**
 * Mixin para a classe Particle e ParticleManager para otimizar o ticking e renderização de partículas.
 * Baseado nos mappings Yarn 1.21.5+build.1
 * Corrigido: Adicionado mixin em ParticleManager para otimização de renderização.
 */
@Mixin({Particle.class, ParticleManager.class}) // Mixin em ambas as classes
public abstract class ParticleMixin {

    // Shadow field para acessar o mundo da partícula (da classe Particle)
    // Garante que o campo `world` é acessado corretamente para a instância de Particle.
    @Shadow(targets = "Lnet/minecraft/client/particle/Particle;world:Lnet/minecraft/client/world/ClientWorld;")
    protected ClientWorld world;


    /**
     * Injeta no início do método tick() da partícula (na classe Particle).
     * Verifica se o tick da partícula deve ser pulado com base na distância (LOD).
     *
     * Target Method Signature (Yarn 1.21.5): Lnet/minecraft/client/particle/Particle;tick()V
     */
    @Inject(
        method = "tick()V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void barium$onParticleTick(CallbackInfo ci) {
        // Converte 'this' para a instância de Particle (quando este mixin é aplicado a Particle)
        Particle self = (Particle)(Object)this;

        // Verifica se o tick deve ser pulado pelo otimizador
        if (ParticleOptimizer.shouldSkipParticleTick(self, this.world)) {
            // Cancela a execução do método tick() original
            ci.cancel();
        }
    }

    /**
     * Injeta no método renderParticles de ParticleManager para otimizar a renderização.
     * Verifica se a partícula deve ser renderizada antes de adicionar ao buffer.
     *
     * Target Class: net.minecraft.client.render.ParticleManager
     * Target Method Signature (Yarn 1.21.5): renderParticles(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;Lnet/minecraft/client/render/LightmapTextureManager;Lnet/minecraft/client/render/Camera;F)V
     *
     * Este `@Inject` é para a classe `ParticleManager`. A local `particle` é capturada do loop interno.
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
        // A própria instância de Particle (`particle`) contém seu mundo (`particle.world`).
        if (!ParticleOptimizer.shouldRenderParticle(particle, camera, particle.world)) { // Use particle.world diretamente
            ci.cancel(); // Cancela a chamada a buildGeometry para esta partícula
        }
    }
}