package com.barium.client.util;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class ChunkCullingUtils {

    /**
     * Verifica se uma seção de chunk (identificada pela sua origem) está 100% cercada
     * por outras seções cujas faces são compostas apenas de blocos que são cubos completos.
     * Retorna true se a seção estiver totalmente ocluída e pode ser pulada.
     *
     * @param world O mundo do cliente.
     * @param sectionOrigin A posição do bloco de origem da seção (geralmente com coordenadas múltiplas de 16).
     * @return true se a seção estiver totalmente ocluída.
     */
    public static boolean isSectionTotallyOccluded(World world, BlockPos sectionOrigin) {
        if (world == null) return false;

        for (Direction faceDirection : Direction.values()) {
            if (!isNeighboringFaceOpaque(world, sectionOrigin, faceDirection)) {
                // Se qualquer uma das 6 faces não estiver bloqueada por uma face opaca, a seção é visível.
                return false;
            }
        }
        // Todas as 6 faces estão bloqueadas, a seção é invisível.
        return true;
    }

    /**
     * Verifica se a face de uma seção vizinha, que está encostada na nossa seção, é composta
     * inteiramente de blocos que são "full cubes".
     *
     * @param world O mundo do cliente.
     * @param ourSectionOrigin A origem da nossa seção.
     * @param direction A direção para olhar (ex: Direction.NORTH).
     * @return true se a face vizinha naquela direção for 100% opaca e sólida.
     */
    public static boolean isNeighboringFaceOpaque(World world, BlockPos ourSectionOrigin, Direction direction) {
        BlockPos neighborSectionOrigin = ourSectionOrigin.add(direction.getOffsetX() * 16, direction.getOffsetY() * 16, direction.getOffsetZ() * 16);

        // A face que precisamos verificar no vizinho é a oposta à direção que estamos olhando.
        Direction faceOnNeighbor = direction.getOpposite();

        // Percorre a face 16x16 do vizinho.
        for (int u = 0; u < 16; u++) {
            for (int v = 0; v < 16; v++) {
                BlockPos blockPosOnFace = getBlockPosOnFace(neighborSectionOrigin, faceOnNeighbor, u, v);
                
                BlockState state = world.getBlockState(blockPosOnFace);

                // --- ESTA É A CORREÇÃO FINAL E CORRETA ---
                // O método isFullCube(BlockView, BlockPos) checa se o bloco é um cubo sólido de 1x1x1.
                // É o método mais adequado para determinar se a visão é completamente bloqueada.
                if (!state.isFullCube(world, blockPosOnFace)) {
                    // Se um único bloco na face vizinha não for um cubo completo, a face não é oclusora.
                    return false;
                }
            }
        }
        // Se todos os 256 blocos na face forem cubos completos, esta face é uma oclusora perfeita.
        return true;
    }

    /**
     * Helper matemático para obter a posição de um bloco em uma das 6 faces de um cubo de 16x16x16.
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