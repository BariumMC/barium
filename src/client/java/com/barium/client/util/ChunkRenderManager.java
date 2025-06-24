package com.barium.client.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Frustum;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;

import java.util.BitSet;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Gerencia o estado dos chunks que devem ser renderizados.
 * <p>
 * Esta classe calcula, a cada frame, um {@link BitSet} que representa uma grade
 * de chunks ao redor do jogador. Cada bit no conjunto corresponde a um chunk,
 * e seu valor (true/false) indica se o chunk está visível (dentro do frustum da câmera).
 * <p>
 * A abordagem com Singleton garante que a instância seja única e facilmente acessível
 * a partir dos Mixins sem a necessidade de passar referências.
 */
public class ChunkRenderManager {

    // --- Implementação do Singleton ---
    private static final ChunkRenderManager INSTANCE = new ChunkRenderManager();

    /**
     * Retorna a instância única do gerenciador.
     */
    public static ChunkRenderManager getInstance() {
        return INSTANCE;
    }
    // ------------------------------------

    // Usa AtomicReference para garantir que a troca do BitSet seja segura entre threads, se necessário.
    private final AtomicReference<BitSet> chunksToRender = new AtomicReference<>(new BitSet());

    // Armazena as dimensões da grade de renderização para acesso rápido.
    private int minRenderChunkX = 0;
    private int minRenderChunkZ = 0;
    private int renderGridSize = 0;

    /**
     * O método principal que calcula quais chunks estão visíveis.
     * Ele é chamado uma vez por frame pelo nosso WorldRendererMixin.
     *
     * @param client  A instância do MinecraftClient.
     * @param frustum O frustum da câmera, usado para os testes de visibilidade.
     */
    public void calculateChunksToRender(MinecraftClient client, Frustum frustum) {
        // Validação para evitar NullPointerExceptions durante o carregamento do mundo.
        if (client.player == null || client.world == null || frustum == null) {
            return;
        }

        BitSet newChunksToRender = new BitSet();
        final int renderDistance = client.options.getViewDistance().getValue();
        final ChunkPos playerChunkPos = client.player.getChunkPos();

        // Define os limites da grade de chunks que vamos testar.
        this.minRenderChunkX = playerChunkPos.x - renderDistance;
        this.minRenderChunkZ = playerChunkPos.z - renderDistance;
        this.renderGridSize = renderDistance * 2 + 1;

        // Itera sobre cada chunk na grade de renderização.
        for (int z = 0; z < this.renderGridSize; z++) {
            for (int x = 0; x < this.renderGridSize; x++) {
                final int chunkX = this.minRenderChunkX + x;
                final int chunkZ = this.minRenderChunkZ + z;

                // Cria uma Bounding Box que representa o volume total de um chunk.
                // CORREÇÃO: Usa a API de dimensão moderna para obter a altura do mundo.
                final Box chunkBox = new Box(
                        chunkX * 16, client.world.getBottomY(), chunkZ * 16,
                        chunkX * 16 + 16, client.world.getDimension().height() + client.world.getBottomY(), chunkZ * 16 + 16
                );

                // Se a caixa do chunk for visível pelo frustum...
                if (frustum.isVisible(chunkBox)) {
                    // ...calculamos seu índice na nossa grade e definimos o bit correspondente como 'true'.
                    final int index = x + z * this.renderGridSize;
                    newChunksToRender.set(index);
                }
            }
        }

        // Atomicamente atualiza o BitSet para que a thread de renderização o use.
        this.chunksToRender.set(newChunksToRender);
    }

    /**
     * Métodos estáticos para acesso conveniente a partir dos Mixins.
     * Eles simplesmente leem os valores da instância única.
     */
    public static BitSet getChunksToRender() {
        return INSTANCE.chunksToRender.get();
    }

    public static int getMinRenderChunkX() {
        return INSTANCE.minRenderChunkX;
    }

    public static int getMinRenderChunkZ() {
        return INSTANCE.minRenderChunkZ;
    }

    public static int getRenderGridSize() {
        return INSTANCE.renderGridSize;
    }
}