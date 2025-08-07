// --- Substitua o conteúdo em: src/client/java/com/barium/client/util/ChunkCullingUtils.java ---
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
     * Verifica se uma seção de chunk está 100% cercada por seções opacas.
     * Esta é a versão aprimorada para o Flood-Fill culling.
     */
    public static boolean isSectionTotallyOccluded(World world, BlockPos sectionOrigin) {
        if (world == null) return false;

        for (var faceDirection : Direction.values()) {
            if (!isNeighboringFaceOpaque(world, sectionOrigin, faceDirection)) {
                return false;
            }
        }
        return true;
    }

    /**
     * **MÉTODO OTIMIZADO**
     * Verifica se a face de uma seção vizinha é inteiramente opaca (composta de "full cubes").
     * Esta é uma parte crítica (hot path) para o algoritmo de Flood-Fill.
     *
     * @return true se a face vizinha for 100% opaca.
     */
    public static boolean isNeighboringFaceOpaque(World world, BlockPos ourSectionOrigin, Direction direction) {
        // Calcula a posição do bloco de origem da seção vizinha.
        BlockPos neighborSectionOrigin = ourSectionOrigin.add(direction.getOffsetX() * 16, direction.getOffsetY() * 16, direction.getOffsetZ() * 16);

        // OTIMIZAÇÃO: Obtém o chunk vizinho uma única vez. Se o chunk não estiver carregado, não podemos
        // considerá-lo opaco, pois pode ser ar, então a visão passa.
        Chunk chunk = world.getChunk(neighborSectionOrigin.getX() >> 4, neighborSectionOrigin.getZ() >> 4);
        if (!(chunk instanceof WorldChunk)) {
            return false;
        }

        // OTIMIZAÇÃO: Obtém a seção relevante do chunk. Se a seção não existir (acima ou abaixo do mundo),
        // ela é efetivamente transparente.
        int sectionY = world.getSectionIndex(neighborSectionOrigin.getY());
        if (sectionY < world.getBottomSectionCoord() || sectionY >= world.getTopSectionCoord()) {
            return false;
        }
        ChunkSection neighborSection = chunk.getSectionArray()[world.sectionCoordToIndex(sectionY)];
        if (neighborSection == null || neighborSection.isEmpty()) {
            return false; // Seção vizinha não existe ou está vazia, visão passa.
        }

        // A face que precisamos verificar no vizinho é a oposta à direção que estamos olhando.
        Direction faceOnNeighbor = direction.getOpposite();

        // Itera pela face 16x16 do vizinho.
        for (int u = 0; u < 16; u++) {
            for (int v = 0; v < 16; v++) {
                BlockPos posOnFace = getBlockPosOnFace(neighborSectionOrigin, faceOnNeighbor, u, v);
                
                // OTIMIZAÇÃO: Usamos getBlockState local da ChunkSection, que é muito mais rápido
                // do que `world.getBlockState()`.
                int localX = posOnFace.getX() & 15;
                int localY = posOnFace.getY() & 15;
                int localZ = posOnFace.getZ() & 15;
                BlockState state = neighborSection.getBlockState(localX, localY, localZ);

                // A verificação de `isFullCube` é a mais correta para oclusão de visão.
                if (!state.isFullCube(chunk, posOnFace)) {
                    return false;
                }
            }
        }
        
        // Se todos os 256 blocos na face forem cubos completos, esta face é uma oclusora perfeita.
        return true;
    }
    
    /**
     * Helper para obter a posição de um bloco em uma das 6 faces de um cubo de 16x16x16.
     * Modernizado com switch expression do Java 17+.
     */
    private static BlockPos getBlockPosOnFace(BlockPos origin, Direction face, int u, int v) {
        return switch (face) {
            case DOWN -> new BlockPos(origin.getX() + u, origin.getY(), origin.getZ() + v);
            case UP -> new BlockPos(origin.getX() + u, origin.getY() + 15, origin.getZ() + v);
            case NORTH -> new BlockPos(origin.getX() + u, origin.getY() + v, origin.getZ());
            case SOUTH -> new BlockPos(origin.getX() + u, origin.getY() + v, origin.getZ() + 15);
            case WEST -> new BlockPos(origin.getX(), origin.getY() + v, origin.getZ() + u);
            case EAST -> new BlockPos(origin.getX() + 15, origin.getY() + v, origin.getZ() + u);
        };
    }
}