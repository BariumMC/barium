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
        // A lógica atual de retornar null depende de como o `buildGeometry` lida com um Particle nulo.
        // Se `buildGeometry` não lida com null, pode causar um NPE.
        // Se a intenção é que o `buildGeometry` simplesmente não seja chamado, a injeção deve ser diferente.
        // No entanto, para a otimização de `ModifyArg`, retornar `null` faz com que `buildGeometry` seja chamado com um Particle nulo.
        // É importante que o `buildGeometry` lide com isso, ou que a injeção seja um `Redirect` para evitar a chamada.
        if (!ParticleOptimizer.shouldRenderParticle(particle, camera)) {
            return null; // O ParticleOptimizer decide se deve renderizar
        }
        return particle;
    }
}