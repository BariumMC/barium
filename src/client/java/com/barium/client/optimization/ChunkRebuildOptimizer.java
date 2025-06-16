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
     * Esta é a versão corrigida que respeita a configuração do usuário.
     *
     * @param section A ChunkSection a ser verificada.
     * @return true se a seção deve ser pulada (é vazia), false caso contrário.
     */
    public static boolean shouldSkipSection(ChunkSection section) {
        // Se a otimização estiver desativada, nós revertemos para a lógica original do vanilla.
        if (!BariumConfig.C.ENABLE_EMPTY_CHUNK_SECTION_CULLING) {
            // A chamada original era section.isEmpty(), então retornamos exatamente isso.
            return section.isEmpty();
        }

        // Se a otimização está LIGADA, usamos nossa lógica aprimorada.
        if (section.isEmpty()) {
            return true;
        }

        // Nossa verificação mais aprofundada.
        for (int x = 0; x < 16; x++) {
            for (int y = 0; y < 16; y++) {
                for (int z = 0; z < 16; z++) {
                    if (!section.getBlockState(x, y, z).isAir()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
}