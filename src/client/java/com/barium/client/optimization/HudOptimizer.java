package com.barium.client.optimization;

import com.barium.BariumMod;
import com.barium.config.BariumConfig;
import net.minecraft.client.MinecraftClient;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.barium.client.optimization.HudStateTracker;
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
     * Esta função não é usada diretamente pelos Mixins de HUD/Debug HUD, mas pode ser usada
     * por futuras otimizações de elementos individuais que precisam de cache.
     *
     * @param hudId Identificador único do elemento da HUD.
     * @param currentContent O conteúdo atual do elemento em String.
     * @return true se o elemento deve ser atualizado/redesenho, false caso contrário.
     */
    public static boolean shouldUpdateHudElement(String hudId, String currentContent) {
        if (!BariumConfig.getInstance().HUD_OPTIMIZATIONS.ENABLE_HUD_CACHING) {
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
     * @param hudId Identificador do elemento da HUD.
     * @return O intervalo de atualização em ticks.
     */
    private static int getUpdateIntervalForHud(String hudId) {
        // Exemplo: Coordenadas e FPS podem ter taxas de atualização diferentes se forem implementados aqui.
        // No momento, o DebugHudMixin lida com a frequência global do F3.
        return BariumConfig.getInstance().HUD_OPTIMIZATIONS.HUD_UPDATE_INTERVAL_TICKS;
    }
    
    /**
     * Verifica se as coordenadas do jogador mudaram significativamente para forçar a atualização do Debug HUD.
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
        
        // Define um pequeno limiar para considerar uma mudança significativa
        double threshold = 0.01; 

        boolean changed = Math.abs(x - lastPlayerX) >= threshold ||
                          Math.abs(y - lastPlayerY) >= threshold ||
                          Math.abs(z - lastPlayerZ) >= threshold;
        
        lastPlayerX = x;
        lastPlayerY = y;
        lastPlayerZ = z;
        
        return changed;
    }

    /**
     * Centraliza a lógica de otimização para a geração das linhas do Debug HUD (F3).
     * Este método é chamado pelo `DebugHudMixin` para as listas da esquerda e da direita.
     *
     * @param debugId Um identificador para o cache ("debug_left" ou "debug_right").
     * @param originalLines A lista de linhas gerada pelo método original do DebugHud (getLeftText/getRightText).
     * @return A lista de linhas a ser exibida (do cache ou a recém-gerada).
     */
    public static List<String> getOptimizedDebugInfo(String debugId, List<String> originalLines) {
        if (!BariumConfig.getInstance().HUD_OPTIMIZATIONS.ENABLE_HUD_CACHING) {
            // Se o cache estiver desabilitado, apenas retorna a lista original.
            cachedDebugInfoMap.put(debugId, originalLines); // Ainda armazena para manter estado se for reabilitado
            return originalLines;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        long currentTick = client.world != null ? client.world.getTime() : 0;
        
        boolean shouldRefreshGlobal = false;
        // A lógica de refresh global é controlada apenas uma vez por tick,
        // geralmente pela primeira chamada (`debug_left`) ou se nenhum cache existir.
        if (debugId.equals("debug_left") || (!cachedDebugInfoMap.containsKey("debug_left") && !cachedDebugInfoMap.containsKey("debug_right"))) { 
            shouldRefreshGlobal = havePlayerCoordinatesChanged(client) || 
                                  (currentTick - lastDebugHudUpdateTime) >= BariumConfig.getInstance().HUD_OPTIMIZATIONS.HUD_UPDATE_INTERVAL_TICKS;
            
            if (shouldRefreshGlobal) {
                lastDebugHudUpdateTime = currentTick; // Reseta o tempo de atualização global
                if (BariumConfig.getInstance().GENERAL_SETTINGS.ENABLE_DEBUG_LOGGING) {
                    BariumMod.LOGGER.debug("Debug HUD global refresh triggered. Tick: " + currentTick);
                }
            }
        }
        
        // Se a decisão global foi para atualizar, ou se este lado ainda não tem cache
        if (shouldRefreshGlobal || !cachedDebugInfoMap.containsKey(debugId)) {
            // Atualiza o cache para este lado do Debug HUD
            // Cria uma *cópia* para evitar modificações externas na lista original,
            // que podem levar a ConcurrentModificationException ou bugs sutis.
            cachedDebugInfoMap.put(debugId, new ArrayList<>(originalLines)); 
            return originalLines; // Retorna a lista original para ser renderizada
        } else {
            // Caso contrário, retorna a versão cacheada
            return cachedDebugInfoMap.get(debugId);
        }
    }
    
    /**
     * Limpa o cache de conteúdo da HUD e as flags de dirty.
     * Deve ser chamado quando há mudanças significativas no estado do jogo (e.g., troca de mundo, desconexão).
     */
    public static void clearHudCache() {
        if (BariumConfig.getInstance().GENERAL_SETTINGS.ENABLE_DEBUG_LOGGING) {
            BariumMod.LOGGER.debug("Limpando cache da HUD.");
        }
        HUD_CONTENT_CACHE.clear();
        UPDATE_COUNTERS.clear();
        cachedDebugInfoMap.clear(); // Limpa o cache específico de informações de depuração
        lastDebugHudUpdateTime = 0;
        lastPlayerX = 0; lastPlayerY = 0; lastPlayerZ = 0; // Reseta as últimas coordenadas
        HudStateTracker.markAllHudDirty(); // Marca todas as flags de dirty como true para forçar redesenho
    }
}