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
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Otimiza a renderização da HUD (F3, placares, etc.), redesenhando apenas quando necessário.
 * Utiliza cache para elementos estáticos ou que mudam com menos frequência.
 * Baseado nos mappings Yarn 1.21.5+build.1
 * Corrigido: Adicionado método init() e sobrecarga para shouldUpdateHudElement().
 * Corrigido: Lógica de cache do DebugHud melhorada para Left/Right texts.
 */
public class HudOptimizer {

    // Cache para linhas de texto da HUD (ex: DebugHud)
    private static final Map<String, List<String>> DEBUG_HUD_TEXT_CACHE = new ConcurrentHashMap<>(); // Armazena listas para "left" e "right"
    private static long lastDebugUpdateTime = 0;
    private static final long DEBUG_UPDATE_INTERVAL_MS = 200; // Atualiza cache do debug a cada 200ms

    // Cache para o estado de outros elementos da HUD
    private static final Map<String, Object> HUD_STATE_CACHE = new ConcurrentHashMap<>();
    private static final Map<String, Long> HUD_UPDATE_TIMESTAMPS = new ConcurrentHashMap<>();
    private static final long DEFAULT_HUD_UPDATE_INTERVAL_MS = 100; // Intervalo padrão para verificar atualização

    /**
     * Método de inicialização (pode ser usado para pré-carregar algo se necessário).
     */
    public static void init() {
        BariumMod.LOGGER.info("Inicializando HudOptimizer");
        clearAllCaches();
    }

    /**
     * Este método não é mais o ponto de injeção primário para cache de linha individual,
     * mas sim um auxiliar se a renderização por linha for otimizada.
     * Mantido para compatibilidade, mas a lógica principal de cache de DebugHud mudou para getCachedDebugHudText.
     */
    @Deprecated // Substituído por getCachedDebugHudText e updateDebugHudCache
    public static boolean tryRenderDebugLineFromCache(String lineContent, DrawContext drawContext, TextRenderer textRenderer, int x, int y, int color) {
        if (!BariumConfig.ENABLE_HUD_OPTIMIZATION || !BariumConfig.CACHE_DEBUG_HUD) {
            return false;
        }
        // A lógica de otimização agora foca em cachear a lista inteira de linhas do DebugHud.
        // Este método não é mais o ponto principal para isso.
        return false;
    }

    /**
     * Atualiza o cache para a lista de linhas do DebugHud.
     * Este método seria chamado após o método original DebugHud.getLeftText() ou getRightText() ter gerado o texto.
     *
     * @param side Esquerda ("left") ou Direita ("right").
     * @param textLines A lista de strings gerada.
     */
    public static void updateDebugHudCache(String side, List<String> textLines) {
        if (!BariumConfig.ENABLE_HUD_OPTIMIZATION || !BariumConfig.CACHE_DEBUG_HUD) {
            return;
        }
        DEBUG_HUD_TEXT_CACHE.put(side, new ArrayList<>(textLines)); // Armazena uma cópia
        lastDebugUpdateTime = System.currentTimeMillis();
        // BariumMod.LOGGER.debug("DebugHud cache updated for side '{}', lines: {}", side, textLines.size());
    }

    /**
     * Obtém a lista de linhas do DebugHud, potencialmente usando cache.
     * Este método interceptaria a chamada original a `DebugHud.getLeftText()` ou `getRightText()`.
     *
     * @param debugHud A instância do DebugHud.
     * @param side Esquerda ("left") ou Direita ("right").
     * @return A lista de strings para a HUD se o cache for válido e deve ser usado; null para indicar que o original deve ser chamado.
     */
    public static List<String> getCachedDebugHudText(DebugHud debugHud, String side) {
        if (!BariumConfig.ENABLE_HUD_OPTIMIZATION || !BariumConfig.CACHE_DEBUG_HUD) {
            return null; // Retorna null para indicar que o original deve ser chamado
        }

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastDebugUpdateTime > DEBUG_UPDATE_INTERVAL_MS) {
            // O cache está desatualizado, indica que o original deve ser chamado para atualizar
            return null;
        } else {
            // Retorna o cache se estiver válido
            List<String> cached = DEBUG_HUD_TEXT_CACHE.get(side);
            if (cached != null) {
                // BariumMod.LOGGER.debug("DebugHud cache hit for side '{}'", side);
                return cached;
            }
            return null; // Cache não existe para este lado, chama o original
        }
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

        HUD_UPDATE_TIMESTAMPS.put(elementKey, currentTime);

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
        HUD_STATE_CACHE.clear();
        HUD_UPDATE_TIMESTAMPS.clear();
        lastDebugUpdateTime = 0;
    }

    // --- Classe interna para o Cache de Linha da HUD ---
    // (Esta classe não é mais usada diretamente para o cache de DebugHud, mas pode ser mantida para outros fins)
    private static class CachedHudLine {
        final String content;
        final long timestamp;

        CachedHudLine(String content) {
            this.content = content;
            this.timestamp = System.currentTimeMillis();
        }

        boolean isValid(String currentContent) {
            // Valida se o conteúdo é o mesmo
            return this.content.equals(currentContent);
        }
    }
}