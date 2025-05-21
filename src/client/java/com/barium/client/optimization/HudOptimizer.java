package com.barium.client.optimization;

import com.barium.BariumMod;
import com.barium.config.BariumConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Otimizador da HUD e textos (Overlay).
 * <p>
 * Implementa:
 * - Redesenho condicional (apenas quando o conteúdo muda ou em intervalos definidos)
 * - Redução da frequência de atualizações da HUD (especialmente Debug HUD)
 */
public class HudOptimizer {
    // Cache de conteúdo da HUD para elementos gerais (fora do F3)
    private static final Map<String, String> HUD_CONTENT_CACHE = new HashMap<>();
    
    // Contadores para controlar a frequência de atualizações de elementos da HUD
    private static final Map<String, Integer> UPDATE_COUNTERS = new HashMap<>();

    // Cache específico para as linhas do Debug HUD (F3)
    // A chave pode ser "debug_left" ou "debug_right"
    private static final Map<String, List<String>> cachedDebugInfoMap = new HashMap<>();
    private static long lastDebugHudUpdateTime = 0; // Para controlar o tempo de atualização global do F3

    // Última posição do jogador para detectar mudanças de coordenadas para o Debug HUD
    private static double lastPlayerX = 0;
    private static double lastPlayerY = 0;
    private static double lastPlayerZ = 0;
    
    public static void init() {
        BariumMod.LOGGER.info("Inicializando otimizações da HUD e textos");
    }
    
    /**
     * Verifica se um elemento da HUD *geral* (que não seja o F3 Debug HUD completo) deve ser atualizado neste frame.
     *
     * @param hudId Identificador único do elemento da HUD.
     * @param currentContent O conteúdo atual do elemento em String.
     * @return true se o elemento deve ser atualizado/redesenho, false caso contrário.
     */
    public static boolean shouldUpdateHudElement(String hudId, String currentContent) {
        if (!BariumConfig.ENABLE_HUD_CACHING) {
            return true;
        }

        String cachedContent = HUD_CONTENT_CACHE.get(hudId);
        boolean contentChanged = cachedContent == null || !cachedContent.equals(currentContent);
        
        int counter = UPDATE_COUNTERS.getOrDefault(hudId, 0) + 1;
        UPDATE_COUNTERS.put(hudId, counter);
        
        int updateInterval = getUpdateIntervalForHud(hudId);
        boolean timeToUpdate = (updateInterval > 0) && (counter >= updateInterval);
        
        boolean shouldUpdate = contentChanged || timeToUpdate;

        if (shouldUpdate) {
            HUD_CONTENT_CACHE.put(hudId, currentContent);
            UPDATE_COUNTERS.put(hudId, 0);
        } else {
            return false;
        }
        
        return true;
    }
    
    /**
     * Determina o intervalo de atualização para um elemento da HUD.
     * 
     * @param hudId Identificador do elemento da HUD.
     * @return O intervalo de atualização em ticks.
     */
    private static int getUpdateIntervalForHud(String hudId) {
        // Coordenadas e FPS são gerados dentro do DebugHud, mas poderiam ser externos
        switch (hudId) {
            case "coordinates": 
                return 2; 
            case "fps":
                return 5;
            default:
                return BariumConfig.HUD_UPDATE_INTERVAL_TICKS;
        }
    }
    
    /**
     * Verifica se as coordenadas do jogador mudaram significativamente.
     * Usado para forçar a atualização do Debug HUD.
     * 
     * @param client O cliente Minecraft.
     * @return true se as coordenadas mudaram, false caso contrário.
     */
    private static boolean havePlayerCoordinatesChanged(MinecraftClient client) {
        if (client.player == null) {
            lastPlayerX = 0; lastPlayerY = 0; lastPlayerZ = 0;
            return false;
        }
        
        double x = client.player.getX();
        double y = client.player.getY();
        double z = client.player.getZ();
        
        boolean changed = Math.abs(x - lastPlayerX) >= 0.01 ||
                          Math.abs(y - lastPlayerY) >= 0.01 ||
                          Math.abs(z - lastPlayerZ) >= 0.01;
        
        lastPlayerX = x;
        lastPlayerY = y;
        lastPlayerZ = z;
        
        return changed;
    }

    /**
     * Centraliza a lógica de otimização para a geração das linhas do Debug HUD (F3).
     * Este método será chamado para as listas da esquerda e da direita separadamente.
     *
     * @param debugId Um identificador para o cache ("debug_left" ou "debug_right").
     * @param originalLines A lista de linhas gerada pelo método original do DebugHud (getLeftText/getRightText).
     * @return A lista de linhas a ser exibida (do cache ou a recém-gerada).
     */
    public static List<String> getOptimizedDebugInfo(String debugId, List<String> originalLines) {
        if (!BariumConfig.ENABLE_HUD_CACHING) {
            // Se o cache estiver desabilitado, apenas retorna a lista original.
            // Também a armazenamos para que shouldRefresh possa funcionar se for reabilitado.
            cachedDebugInfoMap.put(debugId, originalLines);
            return originalLines;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        long currentTick = client.world != null ? client.world.getTime() : 0;
        
        // Verifica se é necessário forçar uma atualização global do F3.
        // A decisão de atualizar é baseada em coordenadas ou no intervalo de tempo configurado.
        // Isso garante que ambos os lados (left/right) sejam atualizados juntos se necessário.
        boolean shouldRefreshGlobal = false;
        if (debugId.equals("debug_left")) { // Apenas uma das chamadas decide se é hora de refresh global
            shouldRefreshGlobal = havePlayerCoordinatesChanged(client) || 
                                  (currentTick - lastDebugHudUpdateTime) >= BariumConfig.HUD_UPDATE_INTERVAL_TICKS;
            
            if (shouldRefreshGlobal) {
                lastDebugHudUpdateTime = currentTick; // Reseta o tempo de atualização global
                BariumMod.LOGGER.debug("Debug HUD global refresh triggered. Tick: " + currentTick);
            }
        }
        
        // Se a decisão global foi para atualizar, ou se este lado ainda não tem cache
        if (shouldRefreshGlobal || !cachedDebugInfoMap.containsKey(debugId)) {
            // Atualiza o cache para este lado do Debug HUD
            cachedDebugInfoMap.put(debugId, new ArrayList<>(originalLines)); // Cria uma cópia para evitar modificações externas
            return originalLines; // Retorna a lista original para ser renderizada
        } else {
            // Caso contrário, retorna a versão cacheada
            return cachedDebugInfoMap.get(debugId);
        }
    }
    
    /**
     * Limpa o cache de conteúdo da HUD.
     * Deve ser chamado quando há mudanças significativas no estado do jogo (e.g., troca de mundo).
     */
    public static void clearHudCache() {
        BariumMod.LOGGER.debug("Limpando cache da HUD.");
        HUD_CONTENT_CACHE.clear();
        UPDATE_COUNTERS.clear();
        cachedDebugInfoMap.clear(); // Limpa o cache específico de informações de depuração
        lastDebugHudUpdateTime = 0;
        lastPlayerX = 0; lastPlayerY = 0; lastPlayerZ = 0; // Reseta as últimas coordenadas
    }
}