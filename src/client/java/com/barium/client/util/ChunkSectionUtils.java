package com.barium.client.util;

import net.minecraft.block.Blocks;
import net.minecraft.world.chunk.ChunkSection;

public class ChunkSectionUtils {

    /**
     * Verifica de forma otimizada se uma ChunkSection contém apenas blocos de ar.
     *
     * @param section A ChunkSection a ser verificada.
     * @return true se a seção estiver vazia (apenas ar), false caso contrário.
     */
    public static boolean isSectionEmpty(ChunkSection section) {
        // Se a seção não existir (por exemplo, abaixo ou acima do mundo), consideramos vazia.
        if (section == null || section.isEmpty()) {
            return true;
        }

        // A verificação mais rápida é iterar e parar no primeiro bloco que não seja ar.
        for (int x = 0; x < 16; x++) {
            for (int y = 0; y < 16; y++) {
                for (int z = 0; z < 16; z++) {
                    // Se encontrarmos qualquer bloco que não seja ar, a seção não está vazia.
                    if (!section.getBlockState(x, y, z).isAir()) {
                        return false;
                    }
                }
            }
        }

        // Se o loop terminar, todos os blocos são ar.
        return true;
    }
}