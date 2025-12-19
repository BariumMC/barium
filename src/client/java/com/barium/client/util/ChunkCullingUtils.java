package com.barium.client.util;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.WorldChunk;

public class ChunkCullingUtils {

    public static boolean isNeighboringFaceOpaque(World world, BlockPos ourSectionOrigin, Direction direction) {
        int nX = ourSectionOrigin.getX() + (direction.getOffsetX() * 16);
        int nY = ourSectionOrigin.getY() + (direction.getOffsetY() * 16);
        int nZ = ourSectionOrigin.getZ() + (direction.getOffsetZ() * 16);

        if (nY < world.getBottomY() || nY >= world.getHeight()) return false;

        Chunk chunk = world.getChunk(nX >> 4, nZ >> 4);
        if (!(chunk instanceof WorldChunk worldChunk)) return false;

        int sectionIndex = world.getSectionIndex(nY);
        ChunkSection[] sections = worldChunk.getSectionArray();
        
        if (sectionIndex < 0 || sectionIndex >= sections.length) return false;
        ChunkSection section = sections[sectionIndex];

        if (section == null || section.isEmpty()) return false;

        // Em vez de loop manual, usamos o predicado do Minecraft que é instantâneo
        // Verificamos se NÃO existe nenhum bloco que NÃO seja opaco (ou seja, se tudo é opaco)
        return !section.getBlockStateContainer().hasAny(state -> !state.isOpaqueFullCube());
    }
}