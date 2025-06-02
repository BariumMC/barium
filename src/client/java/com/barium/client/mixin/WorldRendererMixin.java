// barium-1.21.5-devs/src/client/java/com/barium/client/mixin/WorldRendererMixin.java
package com.barium.client.mixin;

import com.barium.BariumMod;
import com.barium.client.optimization.GeometricOptimizer;
import com.barium.config.BariumConfig;
import net.minecraft.client.MinecraftClient; // Adicionar import
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.chunk.ChunkBuilder;
import net.minecraft.client.render.chunk.ChunkBuilder.BuiltChunk; // Adicionar import
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.ChunkSectionPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin para WorldRenderer para implementar oclusão culling e ganchos para renderização instanciada/impostor.
 * Baseado nos mappings Yarn 1.21.5+build.1.
 */
@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin {

    // Remover estes campos. Acesso via MinecraftClient.getInstance().
    // @Shadow private GameRenderer gameRenderer;
    // @Shadow private Camera camera;
    @Shadow private ChunkBuilder chunkBuilder; // Este campo parece correto no WorldRenderer.

    /**
     * Injeta no método render para executar o pre-pass de occlusion culling
     * ANTES que os chunks sejam iterados para renderização.
     *
     * Target Method: render(Lnet/minecraft/client/util/math/MatrixStack;FJZLnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/GameRenderer;Lnet/minecraft/client/render/LightmapTextureManager;Lorg/joml/Matrix4f;)V
     *
     * A injeção precisa ser antes da iteração sobre as `ChunkInfo` visíveis.
     * O `WorldRenderer.render` é bem longo. Procuramos o ponto onde a lista de `chunkInfos` é obtida e iterada.
     *
     * Injetamos no INVOKE a `GameRenderer.getFov` que ocorre relativamente cedo no método render,
     * e após o frustum culling ser configurado.
     */
    @Inject(
        method = "render(Lnet/minecraft/client/util/math/MatrixStack;FJZLnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/GameRenderer;Lnet/minecraft/client/render/LightmapTextureManager;Lorg/joml/Matrix4f;)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/GameRenderer;getFov(Lnet/minecraft/client/render/Camera;FZ)D", ordinal = 0)
        // Removido locals = LocalCapture.CAPTURE_FAILHARD e os parâmetros locais (SortedSet, BuiltChunk etc.)
    )
    private void barium$beforeChunkRendering(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, org.joml.Matrix4f positionMatrix, CallbackInfo ci) {
        if (BariumConfig.ENABLE_GEOMETRIC_OPTIMIZATION && BariumConfig.ENABLE_CHUNK_OCCLUSION_CULLING) {
            // Esta injeção serve como um gancho para indicar o início de um ciclo de renderização.
            // A lógica real de oclusão para chunks pode ser mais complexa e talvez precise de
            // um processamento em thread separada ou em um ponto de injeção diferente,
            // onde a lista de chunks visíveis é acessível de forma mais segura.
            GeometricOptimizer.advanceFrame(); // Avance o contador de frames do otimizador.
            BariumMod.LOGGER.debug("WorldRendererMixin: Pre-pass occlusion trigger point reached (simplified).");
        }
    }

    /**
     * Redireciona a chamada para renderizar um BuiltChunk para aplicar oclusão culling.
     *
     * Target Method: render(Lnet/minecraft/client/util/math/MatrixStack;FJZLnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/GameRenderer;Lnet/minecraft/client/render/LightmapTextureManager;Lorg/joml/Matrix4f;)V
     * Target INVOKE: Lnet/minecraft/client/render/chunk/ChunkBuilder$BuiltChunk;render(Lnet/minecraft/client/render/RenderLayer;Lnet/minecraft/client/render/VertexConsumer;)V
     *
     * Precisamos encontrar a chamada exata para `BuiltChunk.render` dentro do loop de renderização dos chunks.
     * Haverá múltiplas chamadas, uma para cada `RenderLayer`.
     */
    @Redirect(
        method = "render(Lnet/minecraft/client/util/math/MatrixStack;FJZLnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/GameRenderer;Lnet/minecraft/client/render/LightmapTextureManager;Lorg/joml/Matrix4f;)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/chunk/ChunkBuilder$BuiltChunk;render(Lnet/minecraft/client/render/RenderLayer;Lnet/minecraft/client/render/VertexConsumer;)V")
    )
    private void barium$redirectBuiltChunkRender(BuiltChunk instance, net.minecraft.client.render.RenderLayer renderLayer, net.minecraft.client.render.VertexConsumer consumer) {
        // Acessa a câmera e o mundo via MinecraftClient.getInstance()
        Camera currentCamera = MinecraftClient.getInstance().gameRenderer.getCamera();
        // Assume getOrigin() retorna BlockPos, que pode ser convertido para ChunkSectionPos
        ChunkSectionPos chunkSectionPos = instance.getOrigin().toChunkSectionPos();

        if (BariumConfig.ENABLE_GEOMETRIC_OPTIMIZATION && BariumConfig.ENABLE_CHUNK_OCCLUSION_CULLING &&
            GeometricOptimizer.shouldCullChunkSection(chunkSectionPos, currentCamera, MinecraftClient.getInstance().world)) {
            // Chunk section foi culling, não chama o render original.
            BariumMod.LOGGER.debug("WorldRendererMixin: Chunk section " + chunkSectionPos + " culled by occlusion.");
            return;
        }
        // Se não foi culling, chama o render original.
        instance.render(renderLayer, consumer);
    }

    /**
     * Injeta no final do método render para chamar a renderização de instancing/impostors
     * e avançar o frame count.
     *
     * Target Method: render(...)
     */
    @Inject(
        method = "render(Lnet/minecraft/client/util/math/MatrixStack;FJZLnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/GameRenderer;Lnet/minecraft/client/render/LightmapTextureManager;Lorg/joml/Matrix4f;)V",
        at = @At("RETURN")
    )
    private void barium$postRender(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, org.joml.Matrix4f positionMatrix, CallbackInfo ci) {
        // Acessa a câmera via MinecraftClient.getInstance()
        GeometricOptimizer.renderInstancedAndImpostorBlocks(MinecraftClient.getInstance().gameRenderer.getCamera());
        // GeometricOptimizer.advanceFrame(); // Já chamado no pre-pass, mas pode ser chamado aqui se a ordem for importante
    }
}