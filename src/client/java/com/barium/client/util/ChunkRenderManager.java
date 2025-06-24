// Em: src/client/java/com/barium/client/util/ChunkRenderManager.java
package com.barium.client.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Frustum;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;

import java.util.BitSet;
import java.util.concurrent.atomic.AtomicReference;

public class ChunkRenderManager {
    // Singleton para fácil acesso
    private static final ChunkRenderManager INSTANCE = new ChunkRenderManager();
    
    private final AtomicReference<BitSet> chunksToRender = new AtomicReference<>(new BitSet());
    private int minRenderChunkX = 0;
    private int minRenderChunkZ = 0;
    private int renderGridSize = 0;

    public static ChunkRenderManager getInstance() {
        return INSTANCE;
    }
    
    public void calculateChunksToRender(MinecraftClient client, Frustum frustum) {
        if (client.player == null || client.world == null || frustum == null) {
            return;
        }

        BitSet newChunksToRender = new BitSet();
        int renderDistance = client.options.getViewDistance().getValue();
        ChunkPos playerChunkPos = client.player.getChunkPos();

        this.minRenderChunkX = playerChunkPos.x - renderDistance;
        this.minRenderChunkZ = playerChunkPos.z - renderDistance;
        this.renderGridSize = renderDistance * 2 + 1;

        for (int x = 0; x < this.renderGridSize; x++) {
            for (int z = 0; z < this.renderGridSize; z++) {
                int chunkX = this.minRenderChunkX + x;
                int chunkZ = this.minRenderChunkZ + z;

                Box chunkBox = new Box(
                    chunkX * 16, client.world.getBottomY(), chunkZ * 16,
                    chunkX * 16 + 16, client.world.getTopY(), chunkZ * 16 + 16
                );
                
                if (frustum.isVisible(chunkBox)) {
                    int index = x + z * this.renderGridSize;
                    newChunksToRender.set(index);
                }
            }
        }
        
        this.chunksToRender.set(newChunksToRender);
    }

    // Métodos estáticos para fácil acesso a partir dos mixins
    public static BitSet getChunksToRender() { return INSTANCE.chunksToRender.get(); }
    public static int getMinRenderChunkX() { return INSTANCE.minRenderChunkX; }
    public static int getMinRenderChunkZ() { return INSTANCE.minRenderChunkZ; }
    public static int getRenderGridSize() { return INSTANCE.renderGridSize; }
}