package com.barium.client.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Frustum;
import net.minecraft.util.math.ChunkPos;
import java.util.BitSet;
import java.util.concurrent.atomic.AtomicReference;

public class ChunkRenderManager {
    private static final ChunkRenderManager INSTANCE = new ChunkRenderManager();
    public static ChunkRenderManager getInstance() { return INSTANCE; }

    private final AtomicReference<BitSet> chunksInFrustum = new AtomicReference<>(new BitSet());
    private int minX, minZ, gridSize;

    public void calculateChunksToRender(MinecraftClient client, Frustum frustum) {
        if (client.player == null || client.world == null) return;

        int renderDistance = client.options.getViewDistance().getValue();
        ChunkPos pPos = client.player.getChunkPos();
        
        this.minX = pPos.x - renderDistance;
        this.minZ = pPos.z - renderDistance;
        this.gridSize = renderDistance * 2 + 1;
        
        float minY = (float)client.world.getBottomY();
        float maxY = (float)client.world.getTopY();

        BitSet newSet = new BitSet(gridSize * gridSize);

        for (int x = 0; x < gridSize; x++) {
            double cX = (minX + x) << 4;
            for (int z = 0; z < gridSize; z++) {
                double cZ = (minZ + z) << 4;
                
                // PERFORMANCE: Usamos isVisible com coordenadas puras em vez de criar um objeto Box
                if (frustum.isVisible((double)cX, (double)minY, (double)cZ, (double)cX + 16, (double)maxY, (double)cZ + 16)) {
                    newSet.set(x + z * gridSize);
                }
            }
        }
        chunksInFrustum.set(newSet);
    }

    public boolean isChunkInFrustum(int chunkX, int chunkZ) {
        int lx = chunkX - minX;
        int lz = chunkZ - minZ;
        if (lx < 0 || lx >= gridSize || lz < 0 || lz >= gridSize) return false;
        return chunksInFrustum.get().get(lx + lz * gridSize);
    }

    public void clear() {
        chunksInFrustum.set(new BitSet());
        gridSize = 0;
    }
}