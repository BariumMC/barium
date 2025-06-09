package com.barium.client.mixin;

import com.barium.client.util.ChunkRenderManager; // Importar o ChunkRenderManager do Barium
import com.barium.config.BariumConfig; // Importar a configuração do Barium
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.chunk.ChunkBuilder; // A classe que representa um BuiltChunk para renderização
import net.minecraft.client.render.chunk.ChunkBuilder.BuiltChunk; // A classe BuiltChunk
import net.minecraft.util.math.BlockPos; // Para o getOrigin()
import net.minecraft.util.math.ChunkPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.BitSet;

/**
 * Mixin class para BuiltChunk para modificar o comportamento de construção e culling de chunks.
 * Impede que chunks invisíveis ou fora da frustum de visão sejam construídos.
 */
@Mixin(ChunkBuilder.BuiltChunk.class) // O BuiltChunk é uma classe aninhada de ChunkBuilder
public class ChunkRenderMixin {

    @Inject(method = "shouldBuild", at = @At("HEAD"), cancellable = true)
    private void barium$onShouldBuild(CallbackInfoReturnable<Boolean> cir) {
        // Verifica se a otimização de culling de chunks está ativada na configuração
        if (!BariumConfig.ENABLE_CHUNK_OPTIMIZATION || !BariumConfig.ENABLE_FRUSTUM_CHUNK_CULLING) {
            return; // Permite que o método original continue se a otimização estiver desativada
        }

        BuiltChunk thisChunk = (BuiltChunk) (Object) this;
        MinecraftClient client = MinecraftClient.getInstance();

        // Se o mundo ou o jogador não estiverem prontos, não faz nada
        if (client.world == null || client.player == null) {
            return;
        }

        // Obtém a posição de origem do chunk (BlockPos) e converte para ChunkPos
        BlockPos origin = thisChunk.getOrigin();
        ChunkPos chunkPos = new ChunkPos(origin);
        
        // Obtém o BitSet de chunks a serem renderizados do ChunkRenderManager
        BitSet chunksToRenderBitSet = ChunkRenderManager.getChunksToRender();

        // Se o BitSet não foi inicializado, não faz nada
        if (chunksToRenderBitSet == null) {
            return;
        }

        // Calcula o índice local do chunk na grade de renderização
        int minChunkX = ChunkRenderManager.getMinRenderChunkX();
        int minChunkZ = ChunkRenderManager.getMinRenderChunkZ(); // CORRIGIDO: Nome do método
        int gridSize = ChunkRenderManager.getRenderGridSize();

        int localX = chunkPos.x - minChunkX;
        int localZ = chunkPos.z - minChunkZ;

        // Verifica se o chunk está dentro dos limites da grade de renderização calculada
        if (localX < 0 || localX >= gridSize || localZ < 0 || localZ >= gridSize) {
            // Se estiver fora da área rastreada, não deve ser construído.
            cir.setReturnValue(false);
            return;
        }

        // Calcula o índice linear no BitSet
        int chunkIndex = localX + localZ * gridSize;

        // Verifica se o índice está dentro dos limites do BitSet
        if (chunkIndex < 0 || chunkIndex >= chunksToRenderBitSet.length()) {
             cir.setReturnValue(false); // Fora dos limites, não construir
             return;
        }
		
        // Define o valor de retorno baseado se o chunk deve ser renderizado
        // Se BitSet.get(index) for false, shouldBuild retorna false.
        cir.setReturnValue(chunksToRenderBitSet.get(chunkIndex));
    }
}