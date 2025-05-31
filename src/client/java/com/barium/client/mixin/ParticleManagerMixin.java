package com.barium.client.mixin;

import com.barium.client.optimization.ParticleOptimizer;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.render.Camera;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ParticleManager.class)
public abstract class ParticleManagerMixin {

    @Shadow @Final protected ClientWorld world; // Para acessar o mundo no shouldRenderParticle

    /**
     * Injeta no método renderParticles antes da chamada a particle.buildGeometry.
     * Permite cancelar a renderização de uma partícula específica se ParticleOptimizer decidir.
     *
     * Target Method: Lnet/minecraft/client/particle/ParticleManager;renderParticles(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;Lnet/minecraft/client/render/LightmapTextureManager;Lnet/minecraft/client/render/Camera;F)V
     * Target INVOKE: Lnet/minecraft/client/particle/Particle;buildGeometry(Lnet/minecraft/client/render/VertexConsumer;Lnet/minecraft/client/render/Camera;F)V
     */
    @Inject(
        method = "renderParticles(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;Lnet/minecraft/client/render/LightmapTextureManager;Lnet/minecraft/client/render/Camera;F)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/particle/Particle;buildGeometry(Lnet/minecraft/client/render/VertexConsumer;Lnet/minecraft/client/render/Camera;F)V",
            shift = At.Shift.BEFORE // Executa antes de chamar buildGeometry
        ),
        cancellable = true,
        locals = LocalCapture.CAPTURE_FAILHARD // Captura variáveis locais, incluindo a instância de Particle
    )
    private void barium$beforeParticleRender(
        // Os parâmetros do método original
        // MatrixStack matrices, VertexConsumerProvider.Immediate consumers, LightmapTextureManager lightmap, Camera camera, float tickDelta, CallbackInfo ci,
        // As variáveis locais que precisamos capturar, como a partícula atual no loop
        CallbackInfo ci, Camera camera, Particle particle) { // Ordem pode variar, verificar com o IDE/mapping viewer
        
        if (!ParticleOptimizer.shouldRenderParticle(particle, camera, this.world)) {
            ci.cancel(); // Se não deve renderizar, cancela a chamada ao buildGeometry da partícula atual
        }
    }
}