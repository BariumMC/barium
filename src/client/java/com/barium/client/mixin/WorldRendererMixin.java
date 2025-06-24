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

@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin {

    @Shadow @Final private MinecraftClient client;

    /**
     * Passo 1: Preparação.
     * Interceptamos a chamada a `setupTerrain` para capturar a instância do Frustum
     * e usá-la para calcular nosso BitSet de chunks visíveis.
     *
     * @param instance A instância de WorldRenderer.
     * @param camera A câmera do jogo.
     * @param frustum O frustum que foi criado e que vamos usar.
     * @param hasForcedFrustum flag do jogo.
     * @param spectator flag do jogo.
     */
    @Inject(
        method = "render",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/render/WorldRenderer;setupTerrain(Lnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/Frustum;ZZ)V",
            shift = At.Shift.AFTER // Injetamos logo APÓS setupTerrain ser chamado.
        )
    )
    private void barium$captureFrustumAndPrepare(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f positionMatrix, CallbackInfo ci) {
        // CORREÇÃO AQUI: Em vez de tentar pegar o frustum do chunkBuilder,
        // nós o pegamos de um campo no WorldRenderer que é atualizado pelo setupTerrain.
        // O Frustum é um parâmetro do setupTerrain, mas após a chamada ele é armazenado.
        // A forma mais fácil é deixar o método como está e corrigir o outro mixin.
        // O problema é que o Frustum não é um campo direto. Vamos usar o do ChunkBuilder se disponível.
        // A API mudou. Vamos consertar o primeiro erro primeiro.
    }
    
    // A correção para o segundo erro está em como obtemos o Frustum. O erro original foi um bom palpite.
    // O frustum é, de fato, passado para o método `setupTerrain`.
    // O erro `getFrustum()` estava no local errado. A correção anterior para o `WorldRendererMixin` estava complexa.
    // Vamos simplificar.
    
    // Remova o mixin anterior (barium$prepareVisibleChunkSet) e substitua por este:
    @Inject(method = "setupTerrain(Lnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/Frustum;ZZ)V", at = @At("HEAD"))
    private void barium$updateChunkRenderManager(Camera camera, Frustum frustum, boolean hasForcedFrustum, boolean spectator, CallbackInfo ci) {
        if (BariumConfig.C.ENABLE_FRUSTUM_CHUNK_CULLING) {
            ChunkRenderManager.getInstance().calculateChunksToRender(this.client, frustum);
        }
    }


    /**
     * Passo 2: Otimização com @Redirect.
     * Este mixin permanece o mesmo, pois sua lógica interna está correta.
     */
    @Redirect(
        method = "renderMain(Lnet/minecraft/client/render/FrameGraphBuilder;Lnet/minecraft/client/render/Frustum;Lnet/minecraft/client/render/Camera;Lorg/joml/Matrix4f;Lcom/mojang/blaze3d/buffers/GpuBufferSlice;ZZLnet/minecraft/client/render/RenderTickCounter;Lnet/minecraft/util/profiler/Profiler;)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/render/Frustum;isVisible(Lnet/minecraft/util/math/Box;)Z"
        )
    )
    private boolean barium$cullChunksWithBitSetRedirect(Frustum frustum, Box box) {
        if (!BariumConfig.C.ENABLE_FRUSTUM_CHUNK_CULLING) {
            return frustum.isVisible(box);
        }

        BitSet chunksToRenderBitSet = ChunkRenderManager.getChunksToRender();
        if (chunksToRenderBitSet == null) {
            return frustum.isVisible(box);
        }

        final int minChunkX = ChunkRenderManager.getMinRenderChunkX();
        final int minChunkZ = ChunkRenderManager.getMinRenderChunkZ();
        final int gridSize = ChunkRenderManager.getRenderGridSize();

        final int chunkX = (int) box.minX >> 4;
        final int chunkZ = (int) box.minZ >> 4;

        final int localX = chunkX - minChunkX;
        final int localZ = chunkZ - minChunkZ;

        if (localX < 0 || localX >= gridSize || localZ < 0 || localZ >= gridSize) {
            return false;
        }

        final int chunkIndex = localX + localZ * gridSize;
        
        return chunksToRenderBitSet.get(chunkIndex);
    }
}