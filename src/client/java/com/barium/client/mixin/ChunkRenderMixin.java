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
    
    // Distância em chunks ao quadrado. Chunks além disso não usarão o Vis-Graph. 12*12 = 144
    private static final long VISIBILITY_CHECK_DISTANCE_CHUNKS_SQ = 12 * 12;

    @Inject(method = "shouldBuild()Z", at = @At("HEAD"), cancellable = true)
    private void barium$onShouldBuild(CallbackInfoReturnable<Boolean> cir) {
        
        final BlockPos origin = this.getOrigin();
        final int chunkX = origin.getX() >> 4;
        final int chunkZ = origin.getZ() >> 4;
        
        // --- LÓGICA HÍBRIDA DE CULLING ---

        // 1. Otimização de Oclusão (Estilo Bedrock) para Chunks Próximos
        if (BariumConfig.C.ENABLE_VISIBILITY_GRAPH_CULLING) {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player != null) {
                final ChunkPos cameraChunkPos = client.player.getChunkPos();

                // Calcula a distância em chunks ao quadrado.
                final long dx = chunkX - cameraChunkPos.x;
                final long dz = chunkZ - cameraChunkPos.z;
                final long distSq = dx * dx + dz * dz;

                // Se o chunk estiver PRÓXIMO, aplica o Vis-Graph (ray-casting).
                if (distSq < VISIBILITY_CHECK_DISTANCE_CHUNKS_SQ) {
                    if (!ChunkVisibilityManager.getInstance().isChunkPotentiallyVisible(chunkX, chunkZ)) {
                        // O chunk está escondido atrás de outros! Cancela a construção.
                        cir.setReturnValue(false);
                        return;
                    }
                }
            }
        }
        
        // 2. Otimização de Frustum (Padrão) para todos os chunks
        // Se o chunk passou na verificação acima (ou não foi verificado), fazemos o culling de frustum.
        if (BariumConfig.C.ENABLE_FRUSTUM_CHUNK_CULLING) {
            BitSet chunksToRenderBitSet = ChunkRenderManager.getChunksToRender();
            // Se o BitSet ainda não foi calculado, não fazemos nada.
            if (chunksToRenderBitSet == null) {
                return;
            }

            // Obtém os dados da grade de renderização calculada pelo ChunkRenderManager
            final int minRenderChunkX = ChunkRenderManager.getMinRenderChunkX();
            final int minRenderChunkZ = ChunkRenderManager.getMinRenderChunkZ();
            final int gridSize = ChunkRenderManager.getRenderGridSize();

            // Calcula a posição local do chunk na nossa grade
            final int localX = chunkX - minRenderChunkX;
            final int localZ = chunkZ - minRenderChunkZ;

            // Se o chunk estiver fora da grade de renderização, ele não deve ser construído.
            if (localX < 0 || localX >= gridSize || localZ < 0 || localZ >= gridSize) {
                cir.setReturnValue(false);
                return;
            }

            // Consulta o BitSet para ver se o frustum da câmera pode "ver" este chunk.
            final int chunkIndex = localX + localZ * gridSize;
            if (!chunksToRenderBitSet.get(chunkIndex)) {
                // O chunk está fora do campo de visão. Cancela a construção.
                cir.setReturnValue(false);
                return;
            }
        }
    }
}