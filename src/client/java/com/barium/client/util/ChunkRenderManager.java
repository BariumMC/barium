package com.barium.client.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Frustum;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;

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

        // Pega valores locais para evitar acessos repetidos
        final int renderDistance = client.options.getViewDistance().getValue();
        final ChunkPos playerPos = client.player.getChunkPos();
        final int pX = playerPos.x;
        final int pZ = playerPos.z;
        
        final int bottomY = client.world.getBottomY();
        final int topY = client.world.getDimension().height() + bottomY;

        this.minRenderChunkX = pX - renderDistance;
        this.minRenderChunkZ = pZ - renderDistance;
        this.renderGridSize = renderDistance * 2 + 1;

        BitSet newChunksToRender = new BitSet(this.renderGridSize * this.renderGridSize);

        // Otimização: Reutiliza uma única Box mutável se possível, ou calcula AABB
        // Como Frustum precisa de Box, criamos a menor quantidade possível de lógica auxiliar
        
        for (int x = 0; x < this.renderGridSize; x++) {
            for (int z = 0; z < this.renderGridSize; z++) {
                int absChunkX = this.minRenderChunkX + x;
                int absChunkZ = this.minRenderChunkZ + z;

                // Criação da Box otimizada
                double minX = absChunkX * 16.0;
                double minZ = absChunkZ * 16.0;
                double maxX = minX + 16.0;
                double maxZ = minZ + 16.0;

                // Verifica intersecção com o Frustum (AABB check rápido)
                // AAPI do Frustum do Minecraft aceita AABB.
                if (frustum.isVisible(new Box(minX, bottomY, minZ, maxX, topY, maxZ))) {
                    newChunksToRender.set(x + z * this.renderGridSize);
                }
            }
        }
        this.chunksInFrustum.set(newChunksToRender);
    }

    public boolean isChunkInFrustum(int chunkX, int chunkZ) {
        // Check rápido de limites antes de acessar o BitSet
        int localX = chunkX - this.minRenderChunkX;
        int localZ = chunkZ - this.minRenderChunkZ;

        if (localX < 0 || localX >= this.renderGridSize || localZ < 0 || localZ >= this.renderGridSize) {
            return false;
        }

        BitSet visibleSet = this.chunksInFrustum.get();
        return visibleSet != null && visibleSet.get(localX + localZ * this.renderGridSize);
    }

    public void clear() {
        this.chunksInFrustum.set(new BitSet());
        this.renderGridSize = 0;
    }
}