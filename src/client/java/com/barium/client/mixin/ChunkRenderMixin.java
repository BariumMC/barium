package com.barium.client.mixin;

import com.barium.client.util.ChunkRenderManager;
import com.barium.client.util.ChunkVisibilityManager;
import com.barium.config.BariumConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.chunk.ChunkBuilder;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.BitSet;

@Mixin(ChunkBuilder.BuiltChunk.class)
public abstract class ChunkRenderMixin {

    @Shadow public abstract BlockPos getOrigin();
    
    // Distância ao quadrado para a verificação. (8 chunks * 16 blocos/chunk)^2
    private static final double VISIBILITY_CHECK_DISTANCE_SQ = 128.0 * 128.0;

    @Inject(method = "shouldBuild()Z", at = @At("HEAD"), cancellable = true)
    private void barium$onShouldBuild(CallbackInfoReturnable<Boolean> cir) {
        // --- LÓGICA HÍBRIDA DE CULLING ---

        // Posição do chunk que está sendo verificado
        final BlockPos origin = this.getOrigin();
        final int chunkX = origin.getX() >> 4;
        final int chunkZ = origin.getZ() >> 4;
        
        // Posição do jogador (ou câmera)
        final BlockPos cameraChunkPos = MinecraftClient.getInstance().player.getChunkPos();

        // Calcula a distância (ao quadrado) do chunk até o jogador.
        final long dx = chunkX - cameraChunkPos.getX();
        final long dz = chunkZ - cameraChunkPos.getZ();
        final long distSq = dx * dx + dz * dz;

        // --- Verificação do Vis-Graph (APENAS PARA CHUNKS PRÓXIMOS) ---
        if (BariumConfig.C.ENABLE_VISIBILITY_GRAPH_CULLING && distSq < (VISIBILITY_CHECK_DISTANCE_SQ / (16*16))) {
            if (!ChunkVisibilityManager.getInstance().isChunkPotentiallyVisible(chunkX, chunkZ)) {
                cir.setReturnValue(false);
                return;
            }
        }
        
        // --- Verificação do Frustum Culling (PARA TODOS OS CHUNKS) ---
        if (BariumConfig.C.ENABLE_FRUSTUM_CHUNK_CULLING) {
            BitSet chunksToRenderBitSet = ChunkRenderManager.getChunksToRender();
            if (chunksToRenderBitSet == null) {
                return; // Deixa o jogo decidir se não tivermos dados
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