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
import org.spongepowered.asm.mixin.injection.Redirect; // Mude de Inject para Redirect

@Mixin(ParticleManager.class)
public abstract class ParticleManagerMixin {

    @Shadow @Final protected ClientWorld world; // Mantenha isso se ainda precisar acessar 'world'

    /**
     * Redireciona a chamada para Particle.buildGeometry() para aplicar otimização de renderização.
     */
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
        // 'instance' é a própria partícula (o 'this' da chamada original Particle.buildGeometry)
        // 'consumer', 'camera', 'tickDelta' são os argumentos passados para buildGeometry

        // Verifica se a partícula deve ser renderizada usando sua lógica otimizada
        if (ParticleOptimizer.shouldRenderParticle(instance, camera, this.world)) {
            // Se deve renderizar, chama o método original
            instance.buildGeometry(consumer, camera, tickDelta);
        }
        // Se não deve renderizar, simplesmente não fazemos nada (pulando a chamada original)
    }
}