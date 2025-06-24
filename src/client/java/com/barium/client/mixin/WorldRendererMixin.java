// --- Substitua o conteúdo em: src/client/java/com/barium/client/mixin/WorldRendererMixin.java ---
package com.barium.client.mixin;

import com.barium.client.util.ChunkRenderManager;
import com.barium.config.BariumConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.chunk.ChunkBuilder;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.BitSet;

/**
 * Mixin para otimização do renderizador de mundo (WorldRenderer).
 * Foco: Frustum culling agressivo para chunks.
 */
@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin {

    // Precisamos de uma sombra para o chunkBuilder para obter o frustum
    @Shadow private ChunkBuilder chunkBuilder;
    
    @Shadow private MinecraftClient client;

    /**
     * Otimização para pular renderização de chunks fora do frustum.
     * Este é o ponto de preparação. Antes de qualquer renderização de chunk,
     * nós calculamos quais chunks estão visíveis.
     */
    @Inject(
        method = "render",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/render/WorldRenderer;setupTerrain(Lnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/Frustum;ZZ)V",
            shift = At.Shift.AFTER // Injetamos logo APÓS o setupTerrain, para que o frustum esteja pronto
        )
    )
    private void barium$prepareVisibleChunkSet(MatrixStack matrices, float tickDelta, 
                                             long limitTime, boolean renderBlockOutline, 
                                             Camera camera, GameRenderer gameRenderer, 
                                             LightmapTextureManager lightmapTextureManager, 
                                             Matrix4f positionMatrix, CallbackInfo ci) {
        
        // Se a otimização estiver ligada, calculamos o BitSet de chunks visíveis.
        if (BariumConfig.C.ENABLE_FRUSTUM_CHUNK_CULLING) {
            // Passamos o frustum que acabou de ser calculado no setupTerrain
            ChunkRenderManager.getInstance().calculateChunksToRender(this.client, this.chunkBuilder.getFrustum());
        }
    }

    /**
     * Otimização principal: Aplica o culling.
     * Injetamos ANTES da chamada a `Frustum.isVisible`. Se o nosso BitSet já diz
     * que o chunk não é visível, cancelamos a verificação e toda a lógica de renderização para ele.
     *
     * @param renderChunk O chunk que está prestes a ser verificado e renderizado.
     * @param ci A CallbackInfo que nos permite cancelar a operação.
     */
    @Inject(
        method = "renderMain(Lnet/minecraft/client/util/math/MatrixStack;Lorg/joml/Matrix4f;FJZLnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/GameRenderer;Lnet/minecraft/client/render/LightmapTextureManager;)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/render/Frustum;isVisible(Lnet/minecraft/util/math/Box;)Z",
            shift = At.Shift.BEFORE
        ),
        cancellable = true,
        locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void barium$cullChunksWithBitSet(
            // Parâmetros do método original (não precisamos usá-los, mas devem estar na assinatura)
            MatrixStack matrices, Matrix4f positionMatrix, float tickDelta, long limitTime,
            boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer,
            LightmapTextureManager lightmapTextureManager,
            // Parâmetros de injeção e locais capturados
            CallbackInfo ci,
            // ... (outras variáveis locais que o Mixin captura automaticamente) ...
            WorldRenderer.RenderChunk renderChunk // Esta é a variável local que nos interessa!
    ) {
        
        if (!BariumConfig.C.ENABLE_FRUSTUM_CHUNK_CULLING) {
            return;
        }

        // Pega o BitSet com os chunks visíveis que calculamos anteriormente.
        BitSet chunksToRenderBitSet = ChunkRenderManager.getChunksToRender();
        if (chunksToRenderBitSet == null) {
            return; // Segurança, caso algo não tenha sido inicializado.
        }

        // Pega as dimensões da nossa grade de renderização.
        final int minChunkX = ChunkRenderManager.getMinRenderChunkX();
        final int minChunkZ = ChunkRenderManager.getMinRenderChunkZ();
        final int gridSize = ChunkRenderManager.getRenderGridSize();

        // Converte a posição do chunk para coordenadas de chunk.
        final BlockPos origin = renderChunk.getOrigin();
        final int chunkX = origin.getX() >> 4;
        final int chunkZ = origin.getZ() >> 4;

        // Calcula a posição local do chunk dentro da nossa grade.
        final int localX = chunkX - minChunkX;
        final int localZ = chunkZ - minChunkZ;

        // Se o chunk estiver fora da nossa grade, ele definitivamente não deve ser renderizado.
        if (localX < 0 || localX >= gridSize || localZ < 0 || localZ >= gridSize) {
            ci.cancel(); // OTIMIZAÇÃO: Pula o resto do código para este chunk.
            return;
        }

        // Calcula o índice no BitSet.
        final int chunkIndex = localX + localZ * gridSize;

        // Se o bit para este chunk não estiver definido no nosso BitSet, cancelamos.
        if (!chunksToRenderBitSet.get(chunkIndex)) {
            ci.cancel(); // OTIMIZAÇÃO: Pula o resto do código para este chunk.
        }
    }
}