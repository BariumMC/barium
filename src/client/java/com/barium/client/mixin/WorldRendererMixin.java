package com.barium.client.mixin;

import com.barium.client.optimization.ChunkOcclusionOptimizer; // Import the correct optimizer
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.chunk.ChunkRenderDispatcher; // Import ChunkRenderDispatcher for ChunkRenderInfo
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

/**
 * Mixin para WorldRenderer para implementar o culling de oclusão de chunks baseado em CPU.
 * Otimizado para Yarn 1.21.5+build.1.
 */
@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin { // This class should be WorldRendererMixin, not TextureUtilMixin

    /**
     * Injeta no início do loop de renderização de chunks em WorldRenderer.render().
     * Aplica a lógica de culling de oclusão baseada em CPU *antes* do culling de frustum padrão do Minecraft.
     * Isso permite que o Barium descarte chunks de forma mais eficiente em algumas condições,
     * reduzindo a carga de trabalho para o culling de frustum e o pipeline de renderização.
     *
     * Target Method (Yarn 1.21.5):
     * render(Lnet/minecraft/client/util/math/MatrixStack;FJLnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/GameRenderer;Lnet/minecraft/client/render/LightmapTextureManager;Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;Lnet/minecraft/util/math/Matrix4f;)V
     *
     * O ponto de injeção é ANTES da chamada a `frustum.isVisible(chunkRenderInfo.getChunkRender().getBox())`,
     * que é a verificação de culling de frustum padrão.
     *
     * @param ci CallbackInfo para cancelar a execução original.
     * @param frustum A instância do Frustum usada para culling padrão (capturada localmente).
     * @param chunkRenderInfo A informação do chunk renderizado atual no loop (capturada localmente).
     */
    @Inject(
        method = "render(Lnet/minecraft/client/util/math/MatrixStack;FJLnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/GameRenderer;Lnet/minecraft/client/render/LightmapTextureManager;Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;Lnet/minecraft/util/math/Matrix4f;)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/render/Frustum;isVisible(Lnet/minecraft/util/math/Box;)Z",
            shift = At.Shift.BEFORE // Injeta antes da chamada a frustum.isVisible()
        ),
        locals = LocalCapture.CAPTURE_FAILHARD, // Essencial para capturar variáveis locais do método
        cancellable = true // Permite cancelar a execução do método original (para pular o chunk)
    )
    private void barium$onBeforeFrustumCulling(
        // Parâmetros do método render (assinatura)
        Object matrixStack, float tickDelta, long limitTime, Camera camera, Object gameRenderer,
        Object lightmapTextureManager, Object immediate, Object matrix4f,
        // Variáveis locais capturadas pelo Mixin
        CallbackInfo ci,
        Frustum frustum, // Variável local 'frustum'
        ChunkRenderDispatcher.ChunkRenderInfo chunkRenderInfo // Variável local 'chunkRenderInfo' no loop
    ) {
        // Aplica a lógica de culling de oclusão baseada em CPU do Barium
        if (!ChunkOcclusionOptimizer.shouldRenderChunk(chunkRenderInfo.getChunkRender(), camera)) {
            // Se o Barium decidir que este chunk deve ser descartado, cancela a execução
            // do método original para esta iteração do loop (i.e., pula este chunk).
            ci.cancel();
        }
    }
}