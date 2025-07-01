package com.barium.client.util;

import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.Direction;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;

public class ChunkCullingUtils {

    /**
     * Verifica se uma seção de chunk está completamente cercada por outras seções opacas.
     * Esta é uma otimização poderosa para renderizadores de CPU, pois evita a reconstrução
     * de chunks que são garantidamente invisíveis (ex: no subsolo profundo).
     *
     * @param world O mundo do cliente.
     * @param sectionX Coordenada X da seção.
     * @param sectionY Coordenada Y da seção.
     * @param sectionZ Coordenada Z da seção.
     * @return true se a seção estiver totalmente ocluída, false caso contrário.
     */
    public static boolean isSectionFullyEnclosed(ClientWorld world, int sectionX, int sectionY, int sectionZ) {
        // Itera sobre todas as 6 direções (cima, baixo, norte, sul, leste, oeste).
        for (Direction direction : Direction.values()) {
            int neighborX = sectionX + direction.getOffsetX();
            int neighborY = sectionY + direction.getOffsetY();
            int neighborZ = sectionZ + direction.getOffsetZ();

            // Verifica se o vizinho está fora dos limites do mundo. Se estiver, não está fechado.
            if (neighborY < world.getBottomSectionCoord() || neighborY >= world.getTopSectionCoord()) {
                return false;
            }

            Chunk neighborChunk = world.getChunk(neighborX, neighborZ);
            ChunkSection neighborSection = neighborChunk.getSection(world.sectionIndexToCoord(neighborY));

            // Se a seção vizinha não for opaca, então a seção atual não está fechada.
            if (!isSectionConsideredOpaque(neighborSection)) {
                return false;
            }
        }

        // Se todos os 6 vizinhos são opacos, a seção está totalmente fechada.
        return true;
    }

    /**
     * Uma heurística rápida para determinar se uma seção de chunk pode ser considerada "opaca".
     * Não precisa ser 100% precisa, mas deve ser rápida.
     *
     * @param section A seção a ser verificada.
     * @return true se a seção provavelmente for opaca.
     */
    private static boolean isSectionConsideredOpaque(ChunkSection section) {
        // Se a seção não existe ou está vazia (só ar), definitivamente não é opaca.
        // O método isEmpty() é uma verificação rápida que o próprio Minecraft usa.
        if (section == null || section.isEmpty()) {
            return false;
        }

        // Uma heurística mais forte seria verificar se a contagem de blocos não-aéreos
        // está acima de um certo limite, mas `!isEmpty()` já é um bom e rápido começo.
        // Se a seção não está vazia, há uma boa chance de ela ser opaca o suficiente para
        // esconder a seção vizinha. Esta é uma troca de precisão por velocidade.
        return true;
    }
}