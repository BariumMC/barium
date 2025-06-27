package com.barium.client.mixin;

import com.barium.client.util.ChunkRenderManager;
import com.barium.client.util.ChunkVisibilityManager;
import com.barium.config.BariumConfig;
import net.minecraft.client.render.chunk.ChunkBuilder;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.BitSet;

@Mixin(ChunkBuilder.BuiltChunk.class)
public abstract class ChunkRenderMixin {

    @Shadow public abstract BlockPos getOrigin();

    @Inject(method = "shouldBuild()Z", at = @At("HEAD"), cancellable = true)
    private void barium$hybridCulling(CallbackInfoReturnable<Boolean> cir) {
        final BlockPos origin = this.getOrigin();
        final int chunkX = origin.getX() >> 4;
        final int chunkZ = origin.getZ() >> 4;

        // Camada 1: Culling de Visibilidade (Agressivo)
        if (BariumConfig.C.ENABLE_VISIBILITY_GRAPH_CULLING) {
            if (!ChunkVisibilityManager.getInstance().isChunkPotentiallyVisible(chunkX, chunkZ)) {
                cir.setReturnValue(false); // Otimização agressiva diz que está escondido.
                return;
            }
        }

        // Camada 2: Culling de Frustum (Seguro)
        if (BariumConfig.C.ENABLE_FRUSTUM_CHUNK_CULLING) {
            BitSet chunksToRenderBitSet = ChunkRenderManager.getChunksToRender();
            if (chunksToRenderBitSet == null) return;

            final int minRenderChunkX = ChunkRenderManager.getMinRenderChunkX();
            final int minRenderChunkZ = ChunkRenderManager.getMinRenderChunkZ();
            final int gridSize = ChunkRenderManager.getRenderGridSize();

            final int localX = chunkX - minRenderChunkX;
            final int localZ = chunkZ - minRenderChunkZ;

            if (localX < 0 || localX >= gridSize || localZ < 0 || localZ >= gridSize) {
                cir.setReturnValue(false); // Fora da grade de renderização.
                return;
            }

            final int chunkIndex = localX + localZ * gridSize;
            if (!chunksToRenderBitSet.get(chunkIndex)) {
                cir.setReturnValue(false); // Fora do campo de visão da câmera.
            }
        }
    }
}