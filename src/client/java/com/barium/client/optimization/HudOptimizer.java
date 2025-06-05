package com.barium.client.optimization;

import com.barium.BariumMod;
import com.barium.config.BariumConfig; // Importar a nova BariumConfig
import net.minecraft.client.MinecraftClient;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Otimiza a renderização da HUD (F3, placares, etc.), redesenhando apenas quando necessário.
 * Inclui batch rendering, skipping por delta de tempo, cache adaptativo e compactação de strings.
 * Baseado nos mappings Yarn 1.21.5+build.1
 */
public class HudOptimizer {

    private static final Map<String, List<String>> DEBUG_HUD_TEXT_CACHE = new ConcurrentHashMap<>();
    private static final Map<String, Long> DEBUG_HUD_TIMESTAMPS = new ConcurrentHashMap<>();
    private static final Map<String, Long> DEBUG_HUD_RENDER_TIMESTAMPS = new ConcurrentHashMap<>();
    private static final long BASE_DEBUG_UPDATE_INTERVAL_MS = 200; // Base: 200ms

    private static final Map<String, Object> HUD_STATE_CACHE = new ConcurrentHashMap<>();
    private static final Map<String, Long> HUD_UPDATE_TIMESTAMPS = new ConcurrentHashMap<>();
    private static final long BASE_HUD_UPDATE_INTERVAL_MS = 100; // Base: 100ms

    /**
     * Inicializa o HudOptimizer.
     */
    public static void init() {
        BariumMod.LOGGER.info("Inicializando HudOptimizer");
        // Se você implementar um sistema de configuração, carregue-o aqui, ou no BariumClient.java
        // BariumConfig.loadConfig(); // Exemplo
        clearAllCaches();
    }

    /**
     * Determina o intervalo de atualização adaptativo com base no FPS atual.
     */
    private static long getAdaptiveInterval(long baseInterval) {
        if (!BariumConfig.adaptiveHudOptimization) return baseInterval; // Usa a configuração
        int fps = MinecraftClient.getInstance().getCurrentFps();
        if (fps < 30) {
            return baseInterval * 2; // Aumenta intervalo em low FPS
        } else if (fps > 90) {
            return baseInterval / 2; // Reduz intervalo em high FPS
        }
        return baseInterval;
    }

    /**
     * Verifica se o Debug HUD deve ser recalculado.
     */
    public static boolean shouldRecalculateDebugHud(String side) {
        if (!BariumConfig.enableHudOptimization || !BariumConfig.cacheDebugHud) return true; // Usa a configuração

        long currentTime = System.currentTimeMillis();
        long lastUpdate = DEBUG_HUD_TIMESTAMPS.getOrDefault(side, 0L);
        long adaptiveInterval = getAdaptiveInterval(BASE_DEBUG_UPDATE_INTERVAL_MS);

        return !DEBUG_HUD_TEXT_CACHE.containsKey(side) || (currentTime - lastUpdate) > adaptiveInterval;
    }

    /**
     * Verifica se deve pular a renderização com base no delta de tempo.
     */
    public static boolean shouldSkipRender(String side) {
        if (!BariumConfig.skipHudRender) return false; // Usa a configuração

        long currentTime = System.currentTimeMillis();
        long lastRender = DEBUG_HUD_RENDER_TIMESTAMPS.getOrDefault(side, 0L);

        if ((currentTime - lastRender) < 16) { // Menos de ~1 frame (a 60fps)
            return true;
        }
        DEBUG_HUD_RENDER_TIMESTAMPS.put(side, currentTime);
        return false;
    }

    /**
     * Retorna o texto em cache.
     */
    public static List<String> getCachedDebugHudText(String side) {
        return DEBUG_HUD_TEXT_CACHE.getOrDefault(side, Collections.emptyList());
    }

    /**
     * Atualiza o cache do Debug HUD.
     */
    public static void updateDebugHudCache(String side, List<String> text) {
        if (!BariumConfig.enableHudOptimization || !BariumConfig.cacheDebugHud) return; // Usa a configuração

        // Compacta as strings, evitando formatações repetidas
        List<String> compacted = new ArrayList<>(text.size());
        for (String line : text) {
            compacted.add(line.intern());
        }

        DEBUG_HUD_TEXT_CACHE.put(side, compacted);
        DEBUG_HUD_TIMESTAMPS.put(side, System.currentTimeMillis());
    }

    /**
     * Verifica se um elemento da HUD deve ser atualizado com base no estado.
     */
    public static boolean shouldUpdateHudElement(String elementKey, Supplier<Object> currentStateSupplier) {
        if (!BariumConfig.enableHudOptimization || !BariumConfig.reduceHudUpdates) return true; // Usa a configuração

        long currentTime = System.currentTimeMillis();
        long lastUpdate = HUD_UPDATE_TIMESTAMPS.getOrDefault(elementKey, 0L);
        long adaptiveInterval = getAdaptiveInterval(BASE_HUD_UPDATE_INTERVAL_MS);

        if (currentTime - lastUpdate < adaptiveInterval) {
            return false;
        }

        Object currentState = currentStateSupplier.get();
        Object cachedState = HUD_STATE_CACHE.get(elementKey);

        HUD_UPDATE_TIMESTAMPS.put(elementKey, currentTime);

        if (Objects.equals(currentState, cachedState)) {
            return false;
        }

        HUD_STATE_CACHE.put(elementKey, currentState);
        return true;
    }

    /**
     * Limpa todos os caches.
     */
    public static void clearAllCaches() {
        DEBUG_HUD_TEXT_CACHE.clear();
        DEBUG_HUD_TIMESTAMPS.clear();
        DEBUG_HUD_RENDER_TIMESTAMPS.clear();
        HUD_STATE_CACHE.clear();
        HUD_UPDATE_TIMESTAMPS.clear();
    }
}