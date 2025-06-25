package com.barium.client.mixin;

import com.barium.client.util.ChunkRenderManager;
import com.barium.client.util.ChunkVisibilityManager; // Importa o novo manager
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
    private void barium$onShouldBuild(CallbackInfoReturnable<Boolean> cir) {
        // --- NOVA OTIMIZAÇÃO: Pré-filtro de visibilidade por Ray-casting ---
        if (BariumConfig.C.ENABLE_VISIBILITY_GRAPH_CULLING) {
            final BlockPos origin = this.getOrigin();
            final int chunkX = origin.getX() >> 4;
            final int chunkZ = origin.getZ() >> 4;
            
            // Se o nosso manager diz que o chunk está provavelmente escondido, pulamos o rebuild.
            if (!ChunkVisibilityManager.getInstance().isChunkPotentiallyVisible(chunkX, chunkZ)) {
                cir.setReturnValue(false); // Retorna 'false' para o método shouldBuild()
                return; // Impede que o resto do código (frustum culling) seja executado.
            }
        }

        // --- OTIMIZAÇÃO EXISTENTE: Frustum Culling ---
        if (!BariumConfig.C.ENABLE_FRUSTUM_CHUNK_CULLING) {
            return;
        }

        BitSet chunksToRenderBitSet = ChunkRenderManager.getChunksToRender();
        if (chunksToRenderBitSet == null) {
            return;
        }

        final int minChunkX = ChunkRenderManager.getMinRenderChunkX();
        final int minChunkZ = ChunkRenderManager.getMinRenderChunkZ();
        final int gridSize = ChunkRenderManager.getRenderGridSize();

        final BlockPos origin = this.getOrigin();
        final int chunkX = origin.getX() >> 4;
        final int chunkZ = origin.getZ() >> 4;

        final int localX = chunkX - minChunkX;
        final int localZ = chunkZ - minChunkZ;

        if (localX < 0 || localX >= gridSize || localZ < 0 || localZ >= gridSize) {
            cir.setReturnValue(false);
            return;
        }

        final int chunkIndex = localX + localZ * gridSize;
        
        cir.setReturnValue(chunksToRenderBitSet.get(chunkIndex));
    }
}