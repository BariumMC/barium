package com.barium.client.mixin;

import com.barium.client.util.ChunkRenderManager;
import com.barium.client.util.ChunkVisibilityManager;
import com.barium.config.BariumConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.chunk.ChunkBuilder;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos; // Import necessário
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.BitSet;

@Mixin(ChunkBuilder.BuiltChunk.class)
public abstract class ChunkRenderMixin {

    @Shadow public abstract BlockPos getOrigin();
    
    // Distância em chunks ao quadrado. 8 chunks = 128 blocos. (8*8)
    private static final long VISIBILITY_CHECK_DISTANCE_CHUNKS_SQ = 8 * 8;

    @Inject(method = "shouldBuild()Z", at = @At("HEAD"), cancellable = true)
    private void barium$onShouldBuild(CallbackInfoReturnable<Boolean> cir) {
        
        final BlockPos origin = this.getOrigin();
        final int chunkX = origin.getX() >> 4;
        final int chunkZ = origin.getZ() >> 4;
        
        // --- LÓGICA HÍBRIDA DE CULLING ---
        if (BariumConfig.C.ENABLE_VISIBILITY_GRAPH_CULLING) {
            // CORREÇÃO: Usamos ChunkPos, que é o tipo de retorno correto.
            final ChunkPos cameraChunkPos = MinecraftClient.getInstance().player.getChunkPos();

            // Calcula a distância em chunks ao quadrado.
            final long dx = chunkX - cameraChunkPos.x; // Usa cameraChunkPos.x
            final long dz = chunkZ - cameraChunkPos.z; // Usa cameraChunkPos.z
            final long distSq = dx * dx + dz * dz;

            // Se o chunk estiver PRÓXIMO, aplica o Vis-Graph.
            if (distSq < VISIBILITY_CHECK_DISTANCE_CHUNKS_SQ) {
                if (!ChunkVisibilityManager.getInstance().isChunkPotentiallyVisible(chunkX, chunkZ)) {
                    cir.setReturnValue(false);
                    return;
                }
            }
        }
        
        // --- Verificação do Frustum Culling (PARA TODOS OS CHUNKS) ---
        if (BariumConfig.C.ENABLE_FRUSTUM_CHUNK_CULLING) {
            BitSet chunksToRenderBitSet = ChunkRenderManager.getChunksToRender();
            if (chunksToRenderBitSet == null) {
                return;
            }

            final int minRenderChunkX = ChunkRenderManager.getMinRenderChunkX();
            final int minRenderChunkZ = ChunkRenderManager.getMinRenderChunkZ();
            final int gridSize = ChunkRenderManager.getRenderGridSize();

            final int localX = chunkX - minRenderChunkX;
            final int localZ = chunkZ - minRenderChunkZ;

            if (localX < 0 || localX >= gridSize || localZ < 0 || localZ >= gridSize) {
                cir.setReturnValue(false);
                return;
            }

            final int chunkIndex = localX + localZ * gridSize;
            cir.setReturnValue(chunksToRenderBitSet.get(chunkIndex));
        }
    }
}