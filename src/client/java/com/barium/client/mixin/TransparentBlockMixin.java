package com.barium.client.mixin;

import com.barium.client.optimization.TransparentBlockOptimizer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.Camera;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin para WorldRenderer para otimizar a renderização de blocos transparentes.
 * Foca em melhorar o sorting ou agrupar draw calls para a camada translúcida.
 * Revisado para compatibilidade com mappings Yarn 1.21.5+build.1.
 * Corrigido: Assinatura do método `renderLayer`.
 */
@Mixin(WorldRenderer.class)
public abstract class TransparentBlockMixin {

    /**
     * Injeta antes do início do desenho da camada de renderização, especificamente para a camada translúcida.
     * Permite que o TransparentBlockOptimizer intervenha antes que os vértices sejam enviados para a GPU.
     *
     * Target Class: net.minecraft.client.render.WorldRenderer
     * Target Method Signature (Yarn 1.21.5+build.1): renderLayer(Lnet/minecraft/client/render/RenderLayer;Lnet/minecraft/client/util/math/MatrixStack;DDDLorg/joml/Matrix4f;Lorg/joml/Matrix4f;)V
     * (Nota: A assinatura tem dois argumentos Matrix4f em 1.21.5: a primeira é a de projeção, a segunda é a matriz modelView/atual do GameRenderer)
     */
    @Inject(
        method = "renderLayer(Lnet/minecraft/client/render/RenderLayer;Lnet/minecraft/client/util/math/MatrixStack;DDDLorg/joml/Matrix4f;Lorg/joml/Matrix4f;)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/render/RenderLayer;startDrawing()V", // Ponto antes de iniciar o desenho da camada
            shift = At.Shift.BEFORE
        )
    )
    private void barium$beforeRenderTranslucentLayer(RenderLayer renderLayer, MatrixStack matrices, double cameraX, double cameraY, double cameraZ, Matrix4f projectionMatrix, Matrix4f modelViewMatrix, CallbackInfo ci) {
        // Verifica se é a camada translúcida que estamos interessados em otimizar
        if (renderLayer == RenderLayer.getTranslucent()) {
            WorldRenderer self = (WorldRenderer)(Object)this;
            Camera camera = MinecraftClient.getInstance().gameRenderer.getCamera(); // Obtém a câmera atual

            // Chama o otimizador para aplicar a lógica de sorting ou batching
            // A implementação real da otimização deve estar em TransparentBlockOptimizer
            TransparentBlockOptimizer.optimizeTranslucentRendering(self, matrices, camera, cameraX, cameraY, cameraZ);

            // Nota: A implementação em TransparentBlockOptimizer precisaria acessar e potencialmente
            // modificar a ordem das tarefas de renderização ou os buffers de vértices antes que
            // o método startDrawing() seja chamado e os buffers sejam finalizados e desenhados.
            // Isso pode ser complexo e exigir acesso a campos internos do WorldRenderer ou VertexConsumerProvider.
        }
    }
}