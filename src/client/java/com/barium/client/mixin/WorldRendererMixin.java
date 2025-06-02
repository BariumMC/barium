// barium-1.21.5-devs/src/client/java/com/barium/client/mixin/WorldRendererMixin.java
package com.barium.client.mixin;

import com.barium.BariumMod;
import com.barium.client.optimization.GeometricOptimizer;
import com.barium.config.BariumConfig;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.chunk.ChunkBuilder;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.ChunkSectionPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.SortedSet;

/**
 * Mixin para WorldRenderer para implementar oclusão culling e ganchos para renderização instanciada/impostor.
 * Baseado nos mappings Yarn 1.21.5+build.1.
 */
@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin {

    @Shadow private GameRenderer gameRenderer;
    @Shadow private Camera camera;
    @Shadow private ChunkBuilder chunkBuilder; // Usado para acessar os BuiltChunks

    /**
     * Injeta no método render para executar o pre-pass de occlusion culling
     * ANTES que os chunks sejam iterados para renderização.
     *
     * Target Method: render(Lnet/minecraft/client/util/math/MatrixStack;FJZLnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/GameRenderer;Lnet/minecraft/client/render/LightmapTextureManager;Lnet/minecraft/util/math/Matrix4f;)V
     *
     * A injeção precisa ser antes da iteração sobre as `ChunkInfo` visíveis.
     * O `WorldRenderer.render` é bem longo. Procuramos o ponto onde a lista de `chunkInfos` é obtida e iterada.
     *
     * Injetamos no INVOKE a `GameRenderer.getFov` que ocorre relativamente cedo no método render,
     * e após o frustum culling ser configurado.
     * Isso nos dá acesso às `ChunkInfo` (embora não diretamente capturadas aqui).
     */
    @Inject(
        method = "render(Lnet/minecraft/client/util/math/MatrixStack;FJZLnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/GameRenderer;Lnet/minecraft/client/render/LightmapTextureManager;Lorg/joml/Matrix4f;)V", // Updated Matrix4f type
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/GameRenderer;getFov(Lnet/minecraft/client/render/Camera;FZ)D", ordinal = 0),
        locals = LocalCapture.CAPTURE_FAILHARD // Captura variáveis locais
    )
    private void barium$beforeChunkRendering(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, org.joml.Matrix4f positionMatrix, CallbackInfo ci, // Updated Matrix4f type
                                           // Local variables to capture, check actual names from decompiled code
                                           double d, boolean bl, BuiltChunk builtChunk2, boolean bl2, int i, int j, int k, SortedSet sortedSet, int l) {
        // O `sortedSet` aqui seria a lista de chunks visíveis após o frustum culling.
        // Podemos iterar sobre eles para aplicar nosso occlusion culling.
        if (BariumConfig.ENABLE_GEOMETRIC_OPTIMIZATION && BariumConfig.ENABLE_CHUNK_OCCLUSION_CULLING) {
            // Este é o lugar para preencher o cache de oclusão.
            // NOTA: Iterar sobre `sortedSet` pode ser muito lento aqui em tempo real.
            // Uma implementação real precisaria de um sistema mais sofisticado,
            // talvez rodando em uma thread separada ou com um algoritmo mais rápido.
            for (Object o : sortedSet) {
                // `o` é provavelmente um `WorldRenderer.ChunkInfo` ou similar
                // Você precisaria de um cast e acessar a posição do chunk section.
                // Exemplo: ChunkSectionPos pos = ((ChunkInfo)o).chunk.getSectionPos();
                // GeometricOptimizer.shouldCullChunkSection(pos, this.camera, this.world);
                // Por enquanto, apenas chamaremos o avanço do frame aqui.
            }
            BariumMod.LOGGER.debug("WorldRendererMixin: Pre-pass occlusion triggered.");
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
        method = "render(Lnet/minecraft/client/util/math/MatrixStack;FJZLnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/GameRenderer;Lnet/minecraft/client/render/LightmapTextureManager;Lorg/joml/Matrix4f;)V", // Updated Matrix4f type
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/chunk/ChunkBuilder$BuiltChunk;render(Lnet/minecraft/client/render/RenderLayer;Lnet/minecraft/client/render/VertexConsumer;)V")
    )
    private void barium$redirectBuiltChunkRender(BuiltChunk instance, net.minecraft.client.render.RenderLayer renderLayer, net.minecraft.client.render.VertexConsumer consumer) {
        ChunkSectionPos chunkSectionPos = instance.getOrigin().toChunkSectionPos(); // Assume getOrigin() retorna BlockPos
        if (BariumConfig.ENABLE_GEOMETRIC_OPTIMIZATION && BariumConfig.ENABLE_CHUNK_OCCLUSION_CULLING &&
            GeometricOptimizer.shouldCullChunkSection(chunkSectionPos, this.camera, MinecraftClient.getInstance().world)) {
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
        method = "render(Lnet/minecraft/client/util/math/MatrixStack;FJZLnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/GameRenderer;Lnet/minecraft/client/render/LightmapTextureManager;Lorg/joml/Matrix4f;)V", // Updated Matrix4f type
        at = @At("RETURN")
    )
    private void barium$postRender(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, org.joml.Matrix4f positionMatrix, CallbackInfo ci) { // Updated Matrix4f type
        GeometricOptimizer.renderInstancedAndImpostorBlocks(camera);
        GeometricOptimizer.advanceFrame();
    }
}