// --- Replace the entire content of: src/client/java/com/barium/client/mixin/WorldRendererMixin.java ---
package com.barium.client.mixin;

import com.barium.client.util.ChunkRenderManager;
import com.barium.config.BariumConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.BitSet;
import java.util.Iterator;

@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin {

    @Shadow @Final private MinecraftClient client;

    /**
     * Passo 1: Preparação.
     * Injetamos no `setupTerrain` para capturar o Frustum e calcular nosso BitSet.
     * Esta parte está funcionando e é a nossa base confiável.
     */
    @Inject(method = "setupTerrain(Lnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/Frustum;ZZ)V", at = @At("HEAD"))
    private void barium$updateChunkRenderManager(Camera camera, Frustum frustum, boolean hasForcedFrustum, boolean spectator, CallbackInfo ci) {
        if (BariumConfig.C.ENABLE_FRUSTUM_CHUNK_CULLING && this.client != null) {
            ChunkRenderManager.getInstance().calculateChunksToRender(this.client, frustum);
        }
    }

    /**
     * Passo 2: Otimização.
     * REMOVIDO: O @Redirect em renderMain era muito frágil.
     * NOVA ABORDAGEM: Injetamos no método `renderLayer`. Este método é chamado para
     * renderizar os chunks de um tipo de layer específico (e.g., solid, translucent).
     * Nós capturamos a variável local 'renderChunk' e usamos nosso BitSet para
     * decidir se pulamos a renderização dele.
     *
     * @param ci A CallbackInfo que nos permite cancelar.
     * @param renderChunk A variável local `RenderChunk` capturada do loop.
     */
    @Inject(
        method = "renderLayer(Lnet/minecraft/client/render/RenderLayer;Lnet/minecraft/client/util/math/MatrixStack;DDDLorg/joml/Matrix4f;)V",
        at = @At(
            value = "INVOKE",
            // Este é o ponto onde o chunk é retirado da fila para ser renderizado.
            // Injetamos logo ANTES de ele ser processado.
            target = "Lnet/minecraft/client/render/chunk/ChunkBuilder$BuiltChunk;getOrigin()Lnet/minecraft/util/math/BlockPos;",
            shift = At.Shift.BEFORE
        ),
        cancellable = true,
        locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void barium$cullChunksWithBitSet(
            // Parâmetros do método original
            RenderLayer renderLayer, MatrixStack matrices, double cameraX, double cameraY, double cameraZ, Matrix4f positionMatrix,
            // Parâmetros da injeção
            CallbackInfo ci,
            // Variáveis locais capturadas (a ordem e tipo devem ser exatos)
            boolean bl, RenderPhase.Transparency transparency, Iterator<?> iterator,
            WorldRenderer.RenderChunk renderChunk // A variável que queremos
    ) {
        if (!BariumConfig.C.ENABLE_FRUSTUM_CHUNK_CULLING) {
            return;
        }

        BitSet chunksToRenderBitSet = ChunkRenderManager.getChunksToRender();
        if (chunksToRenderBitSet == null) {
            return; // Segurança
        }

        final int minChunkX = ChunkRenderManager.getMinRenderChunkX();
        final int minChunkZ = ChunkRenderManager.getMinRenderChunkZ();
        final int gridSize = ChunkRenderManager.getRenderGridSize();

        final BlockPos origin = renderChunk.getOrigin();
        final int chunkX = origin.getX() >> 4;
        final int chunkZ = origin.getZ() >> 4;

        final int localX = chunkX - minChunkX;
        final int localZ = chunkZ - minChunkZ;

        if (localX < 0 || localX >= gridSize || localZ < 0 || localZ >= gridSize) {
            ci.cancel(); // Pula a renderização se estiver fora da nossa grade
            return;
        }

        final int chunkIndex = localX + localZ * gridSize;

        // Se o bit não estiver definido, cancelamos a renderização para este chunk.
        if (!chunksToRenderBitSet.get(chunkIndex)) {
            ci.cancel();
        }
    }
}