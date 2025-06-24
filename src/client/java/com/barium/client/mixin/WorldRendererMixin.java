// --- Substitua o conteúdo em: src/client/java/com/barium/client/mixin/WorldRendererMixin.java ---
package com.barium.client.mixin;

import com.barium.client.util.ChunkRenderManager;
import com.barium.config.BariumConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.chunk.ChunkBuilder;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Box;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.BitSet;

/**
 * Mixin para otimização do renderizador de mundo (WorldRenderer).
 * Foco: Frustum culling agressivo para chunks.
 */
@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin {

    @Shadow @Final private MinecraftClient client;
    @Shadow private ChunkBuilder chunkBuilder;

    /**
     * Passo 1: Preparação.
     * Antes de qualquer renderização de chunk, calculamos quais chunks estão visíveis.
     * Esta parte permanece a mesma e está correta.
     */
    @Inject(
        method = "render",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/render/WorldRenderer;setupTerrain(Lnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/Frustum;ZZ)V",
            shift = At.Shift.AFTER
        )
    )
    private void barium$prepareVisibleChunkSet(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f positionMatrix, CallbackInfo ci) {
        if (BariumConfig.C.ENABLE_FRUSTUM_CHUNK_CULLING && this.client != null) {
            ChunkRenderManager.getInstance().calculateChunksToRender(this.client, this.chunkBuilder.getFrustum());
        }
    }

    /**
     * Passo 2: Otimização com @Redirect.
     * Agora que temos a assinatura exata de 'renderMain', podemos redirecionar a chamada
     * a 'Frustum.isVisible' para a nossa própria lógica, que é muito mais rápida.
     *
     * @param frustum A instância do Frustum na qual 'isVisible' seria chamada.
     * @param box A Bounding Box do chunk que está sendo testada.
     * @return true se o chunk deve ser renderizado, false caso contrário.
     */
    @Redirect(
        // Usamos o seletor exato que você forneceu para o método 'renderMain'.
        method = "renderMain(Lnet/minecraft/client/render/FrameGraphBuilder;Lnet/minecraft/client/render/Frustum;Lnet/minecraft/client/render/Camera;Lorg/joml/Matrix4f;Lcom/mojang/blaze3d/buffers/GpuBufferSlice;ZZLnet/minecraft/client/render/RenderTickCounter;Lnet/minecraft/util/profiler/Profiler;)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/render/Frustum;isVisible(Lnet/minecraft/util/math/Box;)Z"
        )
    )
    private boolean barium$cullChunksWithBitSetRedirect(Frustum frustum, Box box) {
        // Se a otimização estiver desligada, apenas chamamos o método original.
        if (!BariumConfig.C.ENABLE_FRUSTUM_CHUNK_CULLING) {
            return frustum.isVisible(box);
        }

        // Pega o nosso BitSet pré-calculado.
        BitSet chunksToRenderBitSet = ChunkRenderManager.getChunksToRender();
        if (chunksToRenderBitSet == null) {
            // Como fallback, usa a lógica original se o nosso manager não estiver pronto.
            return frustum.isVisible(box);
        }

        // Pega as dimensões da nossa grade.
        final int minChunkX = ChunkRenderManager.getMinRenderChunkX();
        final int minChunkZ = ChunkRenderManager.getMinRenderChunkZ();
        final int gridSize = ChunkRenderManager.getRenderGridSize();

        // Extrai as coordenadas do chunk a partir da sua Bounding Box.
        final int chunkX = (int) box.minX >> 4;
        final int chunkZ = (int) box.minZ >> 4;

        // Calcula a posição local na grade.
        final int localX = chunkX - minChunkX;
        final int localZ = chunkZ - minChunkZ;

        // Verifica se está dentro dos limites da grade.
        if (localX < 0 || localX >= gridSize || localZ < 0 || localZ >= gridSize) {
            return false; // Fora da nossa área de interesse, não renderiza.
        }

        // Calcula o índice e verifica no BitSet.
        final int chunkIndex = localX + localZ * gridSize;
        
        // Retorna true se o bit estiver definido, false caso contrário.
        // Isso substitui completamente a chamada a `frustum.isVisible(box)`.
        return chunksToRenderBitSet.get(chunkIndex);
    }
}