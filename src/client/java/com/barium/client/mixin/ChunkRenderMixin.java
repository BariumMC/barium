// --- Substitua o conteúdo em: src/client/java/com/barium/client/mixin/ChunkRenderMixin.java ---
package com.barium.client.mixin;

import com.barium.client.util.ChunkRenderManager;
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
        // CORREÇÃO: Removemos a verificação da variável ENABLE_CHUNK_OPTIMIZATION,
        // que não existe mais. A única verificação necessária aqui é a de frustum culling.
        if (!BariumConfig.ENABLE_FRUSTUM_CHUNK_CULLING) {
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