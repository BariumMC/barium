package com.barium.client.optimization;

import com.barium.BariumMod;
import com.barium.config.BariumConfig;
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
        clearAllCaches();
    }

    /**
     * Determina o intervalo de atualização adaptativo com base no FPS atual.
     */
    private static long getAdaptiveInterval(long baseInterval) {
        if (!BariumConfig.ADAPTIVE_HUD_OPTIMIZATION) return baseInterval;
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
        // Usa as flags de configuração para habilitar/desabilitar as otimizações
        if (!BariumConfig.ENABLE_HUD_OPTIMIZATION || !BariumConfig.CACHE_DEBUG_HUD) return true;

        long currentTime = System.currentTimeMillis();
        long lastUpdate = DEBUG_HUD_TIMESTAMPS.getOrDefault(side, 0L);
        long adaptiveInterval = getAdaptiveInterval(BASE_DEBUG_UPDATE_INTERVAL_MS);

        return !DEBUG_HUD_TEXT_CACHE.containsKey(side) || (currentTime - lastUpdate) > adaptiveInterval;
    }

    /**
     * Verifica se deve pular a renderização com base no delta de tempo.
     * Esta função agora considera especificamente a flag para Debug HUD.
     */
    public static boolean shouldSkipRender(String side) {
        // Apenas aplica o SKIP_HUD_RENDER se ele for para o Debug HUD E a flag SKIP_DEBUG_HUD_RENDER for verdadeira
        if (side.startsWith("debug_") && !BariumConfig.SKIP_DEBUG_HUD_RENDER) {
            return false;
        }
        // Para outros elementos da HUD, usa a flag geral SKIP_HUD_RENDER
        if (!BariumConfig.SKIP_HUD_RENDER && !side.startsWith("debug_")) {
             return false;
        }

        long currentTime = System.currentTimeMillis();
        long lastRender = DEBUG_HUD_RENDER_TIMESTAMPS.getOrDefault(side, 0L);

        // Se o FPS for muito baixo, podemos permitir que o HUD atualize mais frequentemente para evitar o piscar
        // Ou, ajustar o intervalo de 16ms. Para começar, manteremos 16ms mas a flag é crucial.
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
        // Usa as flags de configuração para habilitar/desabilitar as otimizações
        if (!BariumConfig.ENABLE_HUD_OPTIMIZATION || !BariumConfig.CACHE_DEBUG_HUD) return;

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
        // Usa as flags de configuração para habilitar/desabilitar as otimizações
        if (!BariumConfig.ENABLE_HUD_OPTIMIZATION || !BariumConfig.REDUCE_HUD_UPDATES) return true;

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