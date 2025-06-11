package com.barium.client.optimization;

import com.barium.BariumMod;
import com.barium.config.BariumConfig;
import net.minecraft.client.MinecraftClient;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class HudOptimizer {

    private static final Map<String, List<String>> DEBUG_HUD_TEXT_CACHE = new ConcurrentHashMap<>();
    private static final Map<String, Long> DEBUG_HUD_TIMESTAMPS = new ConcurrentHashMap<>();
    private static final long BASE_DEBUG_UPDATE_INTERVAL_MS = 200;

    public static void init() {
        BariumMod.LOGGER.info("Inicializando HudOptimizer");
        clearAllCaches();
    }

    public static boolean shouldRecalculateDebugHud(String side) {
        if (!BariumConfig.C.ENABLE_HUD_OPTIMIZATION || !BariumConfig.C.CACHE_DEBUG_HUD) return true;

        long currentTime = System.currentTimeMillis();
        long lastUpdate = DEBUG_HUD_TIMESTAMPS.getOrDefault(side, 0L);
        return !DEBUG_HUD_TEXT_CACHE.containsKey(side) || (currentTime - lastUpdate) > BASE_DEBUG_UPDATE_INTERVAL_MS;
    }

    public static List<String> getCachedDebugHudText(String side) {
        return DEBUG_HUD_TEXT_CACHE.getOrDefault(side, Collections.emptyList());
    }

    public static void updateDebugHudCache(String side, List<String> text) {
        if (!BariumConfig.C.ENABLE_HUD_OPTIMIZATION || !BariumConfig.C.CACHE_DEBUG_HUD) return;
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

    public static boolean shouldSkipRender(String side) {
        // A lógica complexa foi removida, pois a otimização principal é o cache.
        // Se a otimização de HUD estiver desligada, nunca pulamos a renderização.
        return !BariumConfig.C.ENABLE_HUD_OPTIMIZATION;
    }

    // A lógica de shouldSkipRender e shouldUpdateHudElement foi removida por ser complexa e de baixo impacto.
}