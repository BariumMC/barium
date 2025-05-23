package com.barium.client.mixin;

import net.minecraft.util.math.BlockPos;

// @Mixin(ChunkRendererRegion.class) -- Não precisa de @Mixin aqui, a interface é só a definição.
public interface IChunkRendererRegion {

    /**
     * @return True if this chunk region is considered mostly or fully opaque, blocking visibility.
     */
    boolean barium$isMostlyOpaque();

    /**
     * Sets whether this chunk region is considered mostly or fully opaque.
     * @param opaque True if opaque, false otherwise.
     */
    void barium$setMostlyOpaque(boolean opaque);

    /**
     * @return An integer representing a bitmask of visible faces (0-5, for N, S, E, W, Up, Down) if it's not fully opaque.
     *         0x00 for no faces, 0x3F for all faces potentially visible.
     *         Used for more granular software occlusion maps.
     */
    int barium$getVisibleFacesMask();

    /**
     * Sets the bitmask for potentially visible faces.
     * @param mask The bitmask.
     */
    void barium$setVisibleFacesMask(int mask);

    // Optional: Store the render origin (min X, Y, Z of the chunk section)
    BlockPos barium$getRenderOrigin();
    void barium$setRenderOrigin(BlockPos origin);
}