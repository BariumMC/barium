package com.barium.client.mixin;

import com.barium.config.BariumConfig;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(WorldRenderer.class)
public class WorldRendererRenderLayerMixin {

    /**
     * SOLUÇÃO DEFINITIVA: Em vez de injetar no método 'renderLayer', nós redirecionamos
     * a CHAMADA para ele. Isso é muito mais robusto e contorna problemas de assinatura.
     * Nós interceptamos a chamada e, se a camada for translúcida e a opção estiver ativa,
     * simplesmente não fazemos nada (não chamamos o método original).
     *
     * @param instance A instância de WorldRenderer.
     * @param renderLayer A camada que seria renderizada.
     * @param matrices O MatrixStack.
     * @param cameraX A posição da câmera.
     * @param cameraY A posição da câmera.
     * @param cameraZ A posição da câmera.
     * @param positionMatrix A matriz de projeção.
     */
    @Redirect(
        method = "render(Lnet/minecraft/client/util/math/MatrixStack;FJZLnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/GameRenderer;Lnet/minecraft/client/render/LightmapTextureManager;Lorg/joml/Matrix4f;)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/render/WorldRenderer;renderLayer(Lnet/minecraft/client/render/RenderLayer;Lnet/minecraft/client/util/math/MatrixStack;DDDLorg/joml/Matrix4f;)V"
        )
    )
    private void barium$redirectRenderLayer(WorldRenderer instance, RenderLayer renderLayer, MatrixStack matrices, double cameraX, double cameraY, double cameraZ, Matrix4f positionMatrix) {
        // Se a otimização estiver desligada, ou se a camada NÃO for a translúcida,
        // chamamos o método original normalmente.
        if (!BariumConfig.C.DISABLE_TRANSLUCENT_RENDERING || !renderLayer.toString().equals("RenderLayer[translucent]")) {
            // A 'instance' é o 'this' de WorldRenderer, então chamamos o método original nela.
            instance.renderLayer(renderLayer, matrices, cameraX, cameraY, cameraZ, positionMatrix);
        }
        // Se a otimização estiver ligada E a camada for translúcida, não fazemos nada,
        // pulando efetivamente a renderização.
    }
}