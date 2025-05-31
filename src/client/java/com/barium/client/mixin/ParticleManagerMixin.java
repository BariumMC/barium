package com.barium.client.mixin;

import com.barium.client.optimization.ParticleOptimizer;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumer; // Novo import necessário
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ParticleManager.class) // Voltou a ser ParticleManager
public abstract class ParticleManagerMixin {

    @Shadow @Final protected ClientWorld world;

    @Redirect(
        method = "render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;Lnet/minecraft/client/render/LightmapTextureManager;Lnet/minecraft/client/render/Camera;F)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/particle/Particle;buildGeometry(Lnet/minecraft/client/render/VertexConsumer;Lnet/minecraft/client/render/Camera;F)V"
        )
    )
    private void barium$redirectBuildGeometry(
        Particle instance, // A instância da partícula na qual buildGeometry() foi chamado
        VertexConsumer consumer, // Parâmetro 1 de buildGeometry
        Camera camera, // Parâmetro 2 de buildGeometry
        float tickDelta // Parâmetro 3 de buildGeometry
    ) {
        if (ParticleOptimizer.shouldRenderParticle(instance, camera, this.world)) {
            // Chama o método original buildGeometry() da partícula
            instance.buildGeometry(consumer, camera, tickDelta);
        }
    }
}