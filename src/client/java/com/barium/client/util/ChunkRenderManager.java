package com.barium.client.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Frustum;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;

import java.util.BitSet;
import java.util.concurrent.atomic.AtomicReference;

public class ChunkRenderManager {
    private static final AtomicReference<BitSet> chunksToRender = new AtomicReference<>(new BitSet());
    private static int minRenderChunkX = 0;
    private static int minRenderChunkZ = 0;
    private static int renderGridSize = 0;

    public void calculateChunksToRender(MinecraftClient client, Frustum frustum) {
        if (client.player == null || client.world == null) {
            return;
        }

        BitSet newChunksToRender = new BitSet();
        int currentRenderDistanceChunks = client.options.getViewDistance().getValue();
        ChunkPos playerChunkPos = client.player.getChunkPos();

        minRenderChunkX = playerChunkPos.x - currentRenderDistanceChunks;
        minRenderChunkZ = playerChunkPos.z - currentRenderDistanceChunks;
        renderGridSize = currentRenderDistanceChunks * 2 + 1;

        for (int x = 0; x < renderGridSize; x++) {
            for (int z = 0; z < renderGridSize; z++) {
                int chunkX = minRenderChunkX + x;
                int chunkZ = minRenderChunkZ + z;

                // Usamos getDimension().height() para obter a altura máxima do mundo.
                Box chunkBox = new Box(chunkX * 16, client.world.getBottomY(), chunkZ * 16,
                        chunkX * 16 + 16, client.world.getDimension().height() + client.world.getBottomY(), chunkZ * 16 + 16);

                if (frustum.isVisible(chunkBox)) {
                    int index = x + z * renderGridSize;
                    newChunksToRender.set(index);
                }
            }
        }
        
        chunksToRender.set(newChunksToRender);
    }

    public static BitSet getChunksToRender() { return chunksToRender.get(); }
    public static int getMinRenderChunkX() { return minRenderChunkX; }
    public static int getMinRenderChunkZ() { return minRenderChunkZ; }
    public static int getRenderGridSize() { return renderGridSize; }
}