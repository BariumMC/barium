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
        // Se a nova otimização estiver desligada, não faz nada.
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

        // Converte a posição deste chunk para coordenadas de chunk.
        final BlockPos origin = this.getOrigin();
        final int chunkX = origin.getX() >> 4;
        final int chunkZ = origin.getZ() >> 4;

        // Calcula a posição local do chunk dentro da nossa grade.
        final int localX = chunkX - minChunkX;
        final int localZ = chunkZ - minChunkZ;

        // Se o chunk estiver fora da nossa grade, ele definitivamente não deve ser construído.
        if (localX < 0 || localX >= gridSize || localZ < 0 || localZ >= gridSize) {
            cir.setReturnValue(false);
            return;
        }

        // Calcula o índice no BitSet.
        final int chunkIndex = localX + localZ * gridSize;

        // Define o valor de retorno do método para o valor no nosso BitSet.
        // Se o chunk estiver na lista (true), o método continua. Se não (false), ele é cancelado.
        cir.setReturnValue(chunksToRenderBitSet.get(chunkIndex));
    }
}