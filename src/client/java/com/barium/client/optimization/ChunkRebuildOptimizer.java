package com.barium.client.optimization;

import com.barium.config.BariumConfig;
import net.minecraft.world.chunk.ChunkSection;

/**
 * Contém a lógica para otimizar a fase de reconstrução (rebuild) de um chunk.
 * A principal otimização aqui é pular seções de chunk que estão vazias (contêm apenas ar),
 * evitando que a CPU gaste tempo processando polígonos para o nada.
 */
public class ChunkRebuildOptimizer {

    /**
     * Verifica se uma determinada seção de chunk deve ser pulada durante a reconstrução.
     * Uma seção deve ser pulada se a otimização estiver ativa e a seção for considerada vazia.
     *
     * @param section A ChunkSection a ser verificada.
     * @return true se a seção deve ser pulada (é vazia), false caso contrário.
     */
    public static boolean shouldSkipSection(ChunkSection section) {
        // Se a otimização estiver desativada, nunca pulamos a seção.
        // A lógica do vanilla (section.isEmpty()) cuidará do básico.
        // (Esta otimização não está em BariumConfig ainda, então vamos assumir que está sempre ativa)
        
        // A verificação `section.isEmpty()` é um primeiro passo rápido que o vanilla faz.
        // Ele verifica se a paleta de blocos contém apenas ar.
        if (section.isEmpty()) {
            return true;
        }

        // Nossa verificação mais aprofundada. Itera por todos os 16*16*16 blocos.
        // Isso é necessário para casos onde a paleta não está vazia, mas a seção ainda assim
        // só contém ar.
        for (int x = 0; x < 16; x++) {
            for (int y = 0; y < 16; y++) {
                for (int z = 0; z < 16; z++) {
                    if (!section.getBlockState(x, y, z).isAir()) {
                        // Encontrou um bloco que não é ar, então a seção não está vazia.
                        return false;
                    }
                }
            }
        }

        // Se o loop terminar sem encontrar nenhum bloco, a seção está de fato vazia.
        return true;
    }
}