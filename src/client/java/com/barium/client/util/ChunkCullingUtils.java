package com.barium.client.util;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.WorldChunk;

public class ChunkCullingUtils {

/**
 * Verifica se a face vizinha é opaca. OTIMIZADO.
 */
public static boolean isNeighboringFaceOpaque(World world, BlockPos ourSectionOrigin, Direction direction) {
    // Posição da origem da seção vizinha
    int nX = ourSectionOrigin.getX() + (direction.getOffsetX() * 16);
    int nY = ourSectionOrigin.getY() + (direction.getOffsetY() * 16);
    int nZ = ourSectionOrigin.getZ() + (direction.getOffsetZ() * 16);

    // Check rápido de altura do mundo
    // CORREÇÃO: Usando getBottomY() + getHeight() pois getTopY() sem args pode ser ambíguo ou inexistente
    int topY = world.getBottomY() + world.getHeight();
    if (nY < world.getBottomY() || nY >= topY) return false;

    // Acessa o chunk vizinho (apenas se carregado)
    Chunk chunk = world.getChunk(nX >> 4, nZ >> 4);
    
    // CORREÇÃO: Removida a chamada chunk.isEmpty() que causava erro de compilação.
    // A verificação 'instanceof WorldChunk' já garante que é um chunk válido com dados.
    if (!(chunk instanceof WorldChunk)) return false;

    // Acessa a seção diretamente pelo índice Y
    int sectionIndex = world.getSectionIndex(nY);
    ChunkSection[] sections = chunk.getSectionArray();
    
    if (sectionIndex < 0 || sectionIndex >= sections.length) return false;
    ChunkSection section = sections[sectionIndex];
    
    if (section == null || section.isEmpty()) return false;

    // Se a seção tem blocos, verificamos a face oposta à direção de entrada
    Direction faceToCheck = direction.getOpposite();

    // Itera sobre a face 16x16
    // Usando coordenadas locais (0-15) para evitar matemática de BlockPos repetida
    int startX = 0, endX = 16;
    int startY = 0, endY = 16;
    int startZ = 0, endZ = 16;

    // Definição dos limites do loop baseada na face
    switch (faceToCheck) {
        case DOWN -> { startY = 0; endY = 0; }      // Y fixo em 0
        case UP -> { startY = 15; endY = 15; }      // Y fixo em 15
        case NORTH -> { startZ = 0; endZ = 0; }     // Z fixo em 0
        case SOUTH -> { startZ = 15; endZ = 15; }   // Z fixo em 15
        case WEST -> { startX = 0; endX = 0; }      // X fixo em 0
        case EAST -> { startX = 15; endX = 15; }    // X fixo em 15
    }

    // Ajuste para loops funcionarem (transforma range de tamanho 0 em tamanho 1)
    if (startX == endX) endX++;
    if (startY == endY) endY++;
    if (startZ == endZ) endZ++;

    for (int y = startY; y < endY; y++) {
        for (int z = startZ; z < endZ; z++) {
            for (int x = startX; x < endX; x++) {
                BlockState state = section.getBlockState(x, y, z);
                // Se encontrar UM buraco (não opaco), a face não oclui a visão.
                if (!state.isOpaque() || !state.isFullCube(world, ourSectionOrigin)) { 
                    return false;
                }
            }
        }
    }
    
    return true;
   }
}