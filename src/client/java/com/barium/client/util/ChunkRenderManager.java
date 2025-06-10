package com.barium.client.util;

// Não precisamos mais da BariumConfig aqui, pois a lógica adaptativa foi removida.
// import com.barium.config.BariumConfig; 
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Frustum;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;

import java.util.BitSet;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Gerencia o cálculo de quais chunks devem ser renderizados usando Frustum Culling real
 * para máxima performance. Esta é a versão final e otimizada.
 */
public class ChunkRenderManager {
    private static final AtomicReference<BitSet> chunksToRender = new AtomicReference<>(new BitSet());
    private static int minRenderChunkX = 0;
    private static int minRenderChunkZ = 0;
    private static int renderGridSize = 0;
    
    // A lógica de shouldRecalculate foi removida, pois a atualização agora é feita
    // a cada frame no WorldRendererMixin, que é a abordagem mais eficiente.

    /**
     * Calcula chunks visíveis usando um Frustum matemático real, que é muito mais rápido.
     * Este método é chamado pelo WorldRendererMixin uma vez por frame.
     *
     * @param client  A instância do cliente Minecraft.
     * @param frustum O frustum de visão da câmera, já calculado pelo jogo.
     */
    public void calculateChunksToRender(MinecraftClient client, Frustum frustum) {
        if (client.player == null || client.world == null) {
            return;
        }

        BitSet newChunksToRender = new BitSet();
        int currentRenderDistanceChunks = client.options.getViewDistance().getValue();
        ChunkPos playerChunkPos = client.player.getChunkPos();

        // Define a grade de chunks a serem verificados com base na distância de renderização.
        minRenderChunkX = playerChunkPos.x - currentRenderDistanceChunks;
        minRenderChunkZ = playerChunkPos.z - currentRenderDistanceChunks;
        renderGridSize = currentRenderDistanceChunks * 2 + 1;

        // Itera sobre a grade de chunks.
        for (int x = 0; x < renderGridSize; x++) {
            for (int z = 0; z < renderGridSize; z++) {
                int chunkX = minRenderChunkX + x;
                int chunkZ = minRenderChunkZ + z;

                // Cria uma Bounding Box para o chunk inteiro (do fundo ao topo do mundo).
                Box chunkBox = new Box(chunkX * 16, client.world.getBottomY(), chunkZ * 16,
                        chunkX * 16 + 16, client.world.getTopY(), chunkZ * 16 + 16);

                // O método isVisible é extremamente otimizado e faz todo o trabalho pesado.
                if (frustum.isVisible(chunkBox)) {
                    int index = x + z * renderGridSize;
                    newChunksToRender.set(index);
                }
            }
        }
        
        // Atualiza atomicamente a lista de chunks a serem renderizados.
        chunksToRender.set(newChunksToRender);
    }

    // --- Métodos de acesso estáticos ---

    public static BitSet getChunksToRender() {
        return chunksToRender.get();
    }

    public static int getMinRenderChunkX() {
        return minRenderChunkX;
    }

    public static int getMinRenderChunkZ() {
        return minRenderChunkZ;
    }

    public static int getRenderGridSize() {
        return renderGridSize;
    }
}