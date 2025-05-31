package com.barium.client.mixin;

import com.barium.BariumMod;
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
// import org.spongepowered.asm.mixin.injection.callback.LocalCapture; // COMENTE ou REMOVA

@Mixin(ParticleManager.class) // <-- Mantenha ParticleManager como alvo
public abstract class ParticleManagerMixin {

    // Se você não for usar 'world' neste método de teste, pode comentar/remover também.
    // Mas para o objetivo final, você precisará dele.
    @Shadow @Final protected ClientWorld world;

    // Método de teste - Injeta no HEAD sem locals ou parâmetros complexos
    @Inject(
        method="render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;Lnet/minecraft/client/render/LightmapTextureManager;Lnet/minecraft/client/render/Camera;F)V",
        at = @At("HEAD"), // Injeta no início do método
        cancellable = true // Ainda pode ser cancelável, mas não vamos cancelar neste teste
    )
    private void barium$onRenderHead(CallbackInfo ci) { // Apenas CallbackInfo ci
        BariumMod.LOGGER.info("barium$onRenderHead injetado com sucesso no ParticleManager!");
        // Não adicione lógica que precise de 'camera' ou 'particle' aqui.
        // ci.cancel(); // Não cancele para não quebrar o jogo no teste
    }

    /*
     * O método original com a lógica de otimização de renderização que estava causando problemas
     * Comente-o completamente por enquanto, para que não seja um alvo do Mixin.
     * Nós o reativaremos depois que o teste acima funcionar.
    @Inject(
        method = "render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;Lnet/minecraft/client/render/LightmapTextureManager;Lnet/minecraft/client/render/Camera;F)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/particle/Particle;buildGeometry(Lnet/minecraft/client/render/VertexConsumer;Lnet/minecraft/client/render/Camera;F)V",
            shift = At.Shift.BEFORE
        ),
        cancellable = true,
        locals = LocalCapture.CAPTURE_FAILHARD // Este é o provável culpado
    )
    private void barium$beforeParticleRender(
        CallbackInfo ci, Camera camera, Particle particle) {
        if (!ParticleOptimizer.shouldRenderParticle(particle, camera, this.world)) {
            ci.cancel();
        }
    }
    */
}