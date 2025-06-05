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

        // Se o BitSet não foi inicializado (improvável após ClientTickEvents), não faz nada
        if (chunksToRenderBitSet == null) {
            return;
        }

        // Calcula o índice local do chunk na grade de renderização
        int minChunkX = ChunkRenderManager.getMinRenderChunkX();
        // CORRIGIDO: O nome do método estava incorreto (HgetMinRenderChunkZ para getMinRenderChunkZ)
        int minChunkZ = ChunkRenderManager.getMinRenderChunkZ(); 
        int gridSize = ChunkRenderManager.getRenderGridSize();

        int localX = chunkPos.x - minChunkX;
        int localZ = chunkPos.z - minChunkZ;

        // Verifica se o chunk está dentro dos limites da grade de renderização calculada
        if (localX < 0 || localX >= gridSize || localZ < 0 || localZ >= gridSize) {
            // Se o chunk estiver fora da área rastreada, ele não deve ser construído pela nossa otimização.
            // Isso evita a construção de chunks muito distantes ou que estão fora do cone de visão.
            cir.setReturnValue(false);
            return;
        }

        // Calcula o índice linear no BitSet
        int chunkIndex = localX + localZ * gridSize;

        // Verifica se o índice está dentro dos limites do BitSet para evitar ArrayIndexOutOfBoundsException
        // Embora o cálculo acima deva garantir isso se gridSize for consistente.
        if (chunkIndex < 0 || chunkIndex >= chunksToRenderBitSet.length()) { // Use length() para o tamanho alocado
             cir.setReturnValue(false); // Chunk fora dos limites do BitSet, não deve ser construído
             return;
        }
		
        // Define o valor de retorno baseado se o chunk deve ser renderizado
        // Se BitSet.get(index) for false, significa que o chunk não está no cone de visão,
        // então shouldBuild retorna false para evitar sua construção.
        cir.setReturnValue(chunksToRenderBitSet.get(chunkIndex));
    }

    // O método calculateChunkIndex helper não é mais necessário aqui, pois
    // a lógica foi integrada diretamente ou substituída por chamadas a ChunkRenderManager.
}
