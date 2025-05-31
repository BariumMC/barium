package com.barium.client.optimization;

import com.barium.BariumMod;
import com.barium.config.BariumConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.DebugHud;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Otimiza a renderização da HUD (F3, placares, etc.), redesenhando apenas quando necessário.
 * Utiliza cache para elementos estáticos ou que mudam com menos frequência.
 * Baseado nos mappings Yarn 1.21.5+build.1
 */
public class HudOptimizer {

    // --- Debug HUD (F3) Cache ---
    // Armazena as listas completas de strings para os lados esquerdo e direito do Debug HUD
    private static final Map<String, List<String>> DEBUG_HUD_TEXT_CACHE = new ConcurrentHashMap<>();
    // Armazena o timestamp da última atualização para cada lado do Debug HUD
    private static final Map<String, Long> DEBUG_HUD_TIMESTAMPS = new ConcurrentHashMap<>();
    private static final long DEBUG_UPDATE_INTERVAL_MS = 200; // Atualiza cache do debug a cada 200ms

    // --- Outros Elementos da HUD (generalizado) ---
    private static final Map<String, Object> HUD_STATE_CACHE = new ConcurrentHashMap<>();
    private static final Map<String, Long> HUD_UPDATE_TIMESTAMPS = new ConcurrentHashMap<>();
    private static final long DEFAULT_HUD_UPDATE_INTERVAL_MS = 100; // Intervalo padrão para verificar atualização

    /**
     * Método de inicialização.
     */
    public static void init() {
        BariumMod.LOGGER.info("Inicializando HudOptimizer");
        clearAllCaches();
    }

    /**
     * Verifica se o Debug HUD para um dado lado precisa ser recalculado (o cache expirou ou não existe).
     *
     * @param side "left" ou "right".
     * @return true se o texto do Debug HUD deve ser gerado novamente, false se o cache é válido.
     */
    public static boolean shouldRecalculateDebugHud(String side) {
        if (!BariumConfig.ENABLE_HUD_OPTIMIZATION || !BariumConfig.CACHE_DEBUG_HUD) {
            return true; // Sempre recalcula se a otimização ou cache estiverem desativados
        }

        long currentTime = System.currentTimeMillis();
        long lastUpdate = DEBUG_HUD_TIMESTAMPS.getOrDefault(side, 0L);

        // Se o cache não existe ou expirou, recalcula
        if (!DEBUG_HUD_TEXT_CACHE.containsKey(side) || (currentTime - lastUpdate) > DEBUG_UPDATE_INTERVAL_MS) {
            return true;
        }
        return false; // Cache é válido
    }

    /**
     * Obtém o texto em cache para um dado lado do Debug HUD.
     *
     * @param side "left" ou "right".
     * @return A lista de strings em cache, ou uma lista vazia se não houver nada no cache.
     */
    public static List<String> getCachedDebugHudText(String side) {
        // Retorna uma cópia defensiva para evitar modificações externas no cache
        return DEBUG_HUD_TEXT_CACHE.getOrDefault(side, Collections.emptyList());
    }

    /**
     * Atualiza o cache do Debug HUD com o texto gerado.
     *
     * @param side "left" ou "right".
     * @param text A lista de strings gerada pelo Debug HUD.
     */
    public static void updateDebugHudCache(String side, List<String> text) {
        if (!BariumConfig.ENABLE_HUD_OPTIMIZATION || !BariumConfig.CACHE_DEBUG_HUD) {
            return;
        }
        DEBUG_HUD_TEXT_CACHE.put(side, text);
        DEBUG_HUD_TIMESTAMPS.put(side, System.currentTimeMillis());
    }

    /**
     * Reduz a frequência de atualização de elementos específicos da HUD com base em limiar numérico.
     *
     * @param currentValue O valor atual (ex: coordenada X).
     * @param previousValue O valor anterior.
     * @param threshold O limiar de mudança.
     * @return true se a atualização deve ocorrer.
     */
    public static boolean shouldUpdateHudElement(double currentValue, double previousValue, double threshold) {
        if (!BariumConfig.ENABLE_HUD_OPTIMIZATION || !BariumConfig.REDUCE_HUD_UPDATES) {
            return true; // Atualiza sempre se a otimização está desligada
        }
        return Math.abs(currentValue - previousValue) > threshold;
    }

    /**
     * Verifica se um elemento da HUD deve ser atualizado com base em seu estado atual e cache.
     * Usado por mixins para pular a renderização se o estado não mudou.
     *
     * @param elementKey Uma chave única identificando o elemento da HUD (ex: "status_effects", "hotbar").
     * @param currentStateSupplier Um fornecedor que retorna o estado atual do elemento (pode ser uma string, hash, etc.).
     * @return true se o elemento deve ser atualizado/renderizado, false se o cache é válido e o estado não mudou.
     */
    public static boolean shouldUpdateHudElement(String elementKey, Supplier<Object> currentStateSupplier) {
        if (!BariumConfig.ENABLE_HUD_OPTIMIZATION || !BariumConfig.REDUCE_HUD_UPDATES) {
            return true; // Atualiza sempre se a otimização está desligada
        }

        long currentTime = System.currentTimeMillis();
        long lastUpdate = HUD_UPDATE_TIMESTAMPS.getOrDefault(elementKey, 0L);

        // Verifica com menos frequência para reduzir o custo da obtenção de estado
        if (currentTime - lastUpdate < DEFAULT_HUD_UPDATE_INTERVAL_MS) {
            return false; // Assume que não mudou desde a última verificação
        }

        Object currentState = currentStateSupplier.get();
        Object cachedState = HUD_STATE_CACHE.get(elementKey);

        HUD_UPDATE_TIMESTAMPS.put(elementKey, currentTime); // Atualiza o timestamp da última verificação

        if (currentState != null && currentState.equals(cachedState)) {
            return false; // Estado não mudou, não precisa atualizar
        }

        // Estado mudou ou não estava no cache, atualiza o cache
        HUD_STATE_CACHE.put(elementKey, currentState);
        return true; // Precisa atualizar
    }

    /**
     * Limpa todos os caches da HUD.
     */
    public static void clearAllCaches() {
        DEBUG_HUD_TEXT_CACHE.clear();
        DEBUG_HUD_TIMESTAMPS.clear();
        HUD_STATE_CACHE.clear();
        HUD_UPDATE_TIMESTAMPS.clear();
    }
}