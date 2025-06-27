package com.barium.client.optimization;

import com.barium.BariumMod;
import com.barium.config.BariumConfig;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class HudOptimizer {

    // Cache para o texto da tela de depuração (F3). A chave pode ser "debug_left" ou "debug_right".
    private static final Map<String, List<String>> DEBUG_HUD_TEXT_CACHE = new ConcurrentHashMap<>();
    
    // Guarda o timestamp da última atualização para cada lado do HUD.
    private static final Map<String, Long> DEBUG_HUD_TIMESTAMPS = new ConcurrentHashMap<>();
    
    // Intervalo de atualização em milissegundos. 200ms = 5 updates por segundo.
    private static final long DEBUG_UPDATE_INTERVAL_MS = 200;

    public static void init() {
        BariumMod.LOGGER.info("Inicializando HudOptimizer");
        clearAllCaches();
    }

    /**
     * Decide se a tela de depuração (F3) deve ser recalculada ou se podemos usar o cache.
     */
    public static boolean shouldRecalculateDebugHud(String side) {
        if (!BariumConfig.C.CACHE_DEBUG_HUD) return true; // Se a otimização estiver desligada

        long currentTime = System.currentTimeMillis();
        long lastUpdate = DEBUG_HUD_TIMESTAMPS.getOrDefault(side, 0L);
        
        // Recalcula se o cache não existir ou se o intervalo de tempo já passou.
        return !DEBUG_HUD_TEXT_CACHE.containsKey(side) || (currentTime - lastUpdate) > DEBUG_UPDATE_INTERVAL_MS;
    }

    /**
     * Retorna o texto em cache para um dos lados da tela F3.
     */
    public static List<String> getCachedDebugHudText(String side) {
        return DEBUG_HUD_TEXT_CACHE.getOrDefault(side, Collections.emptyList());
    }

    /**
     * Atualiza o cache com o novo texto gerado pelo jogo.
     */
    public static void updateDebugHudCache(String side, List<String> text) {
        if (!BariumConfig.C.CACHE_DEBUG_HUD) return;
        
        // Usar .intern() em strings pode economizar memória se houver muitas strings repetidas.
        List<String> compacted = new ArrayList<>(text.size());
        for (String line : text) {
            compacted.add(line.intern());
        }
        
        DEBUG_HUD_TEXT_CACHE.put(side, compacted);
        DEBUG_HUD_TIMESTAMPS.put(side, System.currentTimeMillis());
    }

    public static void clearAllCaches() {
        DEBUG_HUD_TEXT_CACHE.clear();
        DEBUG_HUD_TIMESTAMPS.clear();
    }
}