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
import org.spongepowered.asm.mixin.Unique; // Adicionado para a demonstração estrutural, mesmo que não usado ativamente
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ParticleManager.class)
public abstract class ParticleManagerMixin {

    @Shadow @Final private ClientWorld world;

    // Adicionado apenas para demonstrar a estrutura similar com um campo '@Unique'.
    // Para esta otimização específica de culling de partículas, este campo não é funcionalmente necessário.
    @Unique
    private Object barium$cachedParticleRenderInfo; // Exemplo de um campo único, sem uso ativo para culling direto aqui

    /**
     * Injeta antes da chamada a Particle.buildGeometry para verificar se uma partícula deve ser renderizada.
     * Se o ParticleOptimizer indicar que a partícula não deve ser renderizada, a chamada original é cancelada.
     * Isso impede que a partícula seja adicionada ao buffer de renderização desnecessariamente.
     *
     * A estrutura de `INVOKE` + `cancellable = true` é similar ao padrão do Sodium para substituição de lógica.
     */
    @Inject(
        method = "renderParticles(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;Lnet/minecraft/client/render/LightmapTextureManager;Lnet/minecraft/client/render/Camera;FLnet/minecraft/util/math/Vec3d;)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/particle/Particle;buildGeometry(Lnet/minecraft/client/render/VertexConsumer;Lnet/minecraft/client/render/Camera;F)V",
            shift = At.Shift.BEFORE // Injete antes do método de construção de geometria ser chamado
        ),
        cancellable = true,
        locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void barium$beforeBuildGeometry(MatrixStack matrices, VertexConsumerProvider.Immediate consumers,
                                            LightmapTextureManager lightmap, Camera camera, float tickDelta,
                                            Vec3d cameraPos, CallbackInfo ci, VertexConsumer buffer,
                                            Particle particle // O `particle` é capturado aqui para nossa verificação
    ) {
        if (!ParticleOptimizer.shouldRenderParticle(particle, camera, this.world)) {
            // Se não deve renderizar, cancela a execução para o buildGeometry para esta partícula.
            ci.cancel();
        }
    }
}