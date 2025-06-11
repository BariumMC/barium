// --- Substitua o conteúdo em: src/main/java/com/barium/config/BariumConfig.java ---
package com.barium.config;

/**
 * Classe principal de acesso às configurações do mod.
 * Ela contém uma única instância estática (C) da classe ConfigData.
 * Esta instância é carregada e gerenciada pelo ConfigManager.
 * Todo o código do mod deve ler as configurações a partir de BariumConfig.C.
 */
public class BariumConfig {

    /**
     * A instância estática que contém todas as configurações carregadas.
     * Inicializada com valores padrão, mas será sobrescrita pelo ConfigManager.
     */
    public static ConfigData C = new ConfigData();
}