package com.barium.client.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Frustum;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.Heightmap;

import java.util.BitSet;
import java.util.concurrent.atomic.AtomicReference;

public class ChunkRenderManager {

    private static final ChunkRenderManager INSTANCE = new ChunkRenderManager();
    public static ChunkRenderManager getInstance() { return INSTANCE; }

    private final AtomicReference<BitSet> chunksInFrustum = new AtomicReference<>(new BitSet());
    private int minRenderChunkX = 0;
    private int minRenderChunkZ = 0;
    private int renderGridSize = 0;

    public void calculateChunksToRender(MinecraftClient client, Frustum frustum) {
        if (client.player == null || client.world == null || frustum == null) return;

        final int renderDistance = client.options.getViewDistance().getValue();
        final ChunkPos playerChunkPos = client.player.getChunkPos();

        this.minRenderChunkX = playerChunkPos.x - renderDistance;
        this.minRenderChunkZ = playerChunkPos.z - renderDistance;
        this.renderGridSize = renderDistance * 2 + 1;

        BitSet newChunksToRender = new BitSet(this.renderGridSize * this.renderGridSize);

        for (int z = 0; z < this.renderGridSize; z++) {
            for (int x = 0; x < this.renderGridSize; x++) {
                final int chunkX = this.minRenderChunkX + x;
                final int chunkZ = this.minRenderChunkZ + z;

                // CORREÇÃO: A caixa do chunk agora usa a altura da dimensão, o que é correto e funciona.
                // Isso cobre o chunk inteiro, da base (geralmente Y=-64) ao topo do mundo.
                final Box chunkBox = new Box(
                        chunkX * 16, client.world.getBottomY(), chunkZ * 16,
                        chunkX * 16 + 16, client.world.getDimension().height() + client.world.getBottomY(), chunkZ * 16 + 16
                );

                if (frustum.isVisible(chunkBox)) {
                    newChunksToRender.set(x + z * this.renderGridSize);
                }
            }
        }
        this.chunksInFrustum.set(newChunksToRender);
    }

    public boolean isChunkInFrustum(int chunkX, int chunkZ) {
        BitSet visibleSet = this.chunksInFrustum.get();
        if (visibleSet == null || this.renderGridSize == 0) return true; // Segurança

        final int localX = chunkX - this.minRenderChunkX;
        final int localZ = chunkZ - this.minRenderChunkZ;

        if (localX < 0 || localX >= this.renderGridSize || localZ < 0 || localZ >= this.renderGridSize) {
            return false;
        }

        return visibleSet.get(localX + localZ * this.renderGridSize);
    }

    public void clear() {
        this.chunksInFrustum.set(new BitSet());
        this.renderGridSize = 0;
    }
}