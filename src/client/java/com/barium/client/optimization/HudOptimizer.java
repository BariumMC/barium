package com.barium.client.optimization;

import com.barium.BariumMod;
import com.barium.config.BariumConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text; // Ainda útil para contexto, mas não para o cache de "Object"

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
    // Cache de conteúdo da HUD para elementos gerais
    private static final Map<String, String> HUD_CONTENT_CACHE = new HashMap<>();
    
    // Contadores para controlar a frequência de atualizações de elementos da HUD
    private static final Map<String, Integer> UPDATE_COUNTERS = new HashMap<>();

    // Cache específico para as linhas do Debug HUD (F3)
    private static List<String> cachedDebugInfoLines = new ArrayList<>();
    private static long lastDebugHudUpdateTime = 0; // Para controlar o tempo de atualização

    // Última posição do jogador para detectar mudanças de coordenadas para o Debug HUD
    private static double lastPlayerX = 0;
    private static double lastPlayerY = 0;
    private static double lastPlayerZ = 0;
    
    public static void init() {
        BariumMod.LOGGER.info("Inicializando otimizações da HUD e textos");
    }
    
    /**
     * Verifica se um elemento da HUD *geral* (que não seja o F3 Debug HUD completo) deve ser atualizado neste frame.
     * Use este método se você tiver outros elementos customizados da HUD que gostaria de otimizar.
     *
     * @param hudId Identificador único do elemento da HUD.
     * @param currentContent O conteúdo atual do elemento em String.
     * @return true se o elemento deve ser atualizado/redesenho, false caso contrário.
     */
    public static boolean shouldUpdateHudElement(String hudId, String currentContent) {
        if (!BariumConfig.ENABLE_HUD_CACHING) {
            return true;
        }

        // Verifica se o conteúdo mudou
        String cachedContent = HUD_CONTENT_CACHE.get(hudId);
        boolean contentChanged = cachedContent == null || !cachedContent.equals(currentContent);
        
        // Incrementa o contador para este elemento
        int counter = UPDATE_COUNTERS.getOrDefault(hudId, 0) + 1;
        UPDATE_COUNTERS.put(hudId, counter);
        
        // Determina a frequência de atualização
        int updateInterval = getUpdateIntervalForHud(hudId);
        
        // Verifica se é hora de atualizar com base no contador
        boolean timeToUpdate = (updateInterval > 0) && (counter >= updateInterval);
        
        // Se o conteúdo mudou OU for hora de atualizar
        boolean shouldUpdate = contentChanged || timeToUpdate;

        if (shouldUpdate) {
            // Atualiza o cache e reseta o contador SOMENTE se uma atualização ocorrer
            HUD_CONTENT_CACHE.put(hudId, currentContent);
            UPDATE_COUNTERS.put(hudId, 0);
        } else {
            // Se não atualizou, retorna false (não precisa redesenhar)
            return false;
        }
        
        return true; // Se chegou aqui, deve redesenhar
    }
    
    /**
     * Determina o intervalo de atualização para um elemento da HUD.
     * 
     * @param hudId Identificador do elemento da HUD.
     * @return O intervalo de atualização em ticks.
     */
    private static int getUpdateIntervalForHud(String hudId) {
        switch (hudId) {
            case "coordinates": // Coordenadas (parte do F3)
                return 2; // Atualiza a cada 2 ticks para ser responsivo
            case "fps": // FPS (parte do F3)
                return 5; // Atualiza a cada 5 ticks
            // "debug_full" será tratado por getOptimizedDebugInfo diretamente
            default:
                return BariumConfig.HUD_UPDATE_INTERVAL_TICKS;
        }
    }
    
    /**
     * Verifica se as coordenadas do jogador mudaram significativamente.
     * Usado principalmente para o Debug HUD.
     * 
     * @param client O cliente Minecraft.
     * @return true se as coordenadas mudaram, false caso contrário.
     */
    public static boolean havePlayerCoordinatesChanged(MinecraftClient client) {
        if (client.player == null) {
            // Se não há jogador, limpa as últimas coordenadas e retorna false
            lastPlayerX = 0; lastPlayerY = 0; lastPlayerZ = 0;
            return false;
        }
        
        double x = client.player.getX();
        double y = client.player.getY();
        double z = client.player.getZ();
        
        // Verifica se houve mudança significativa (pelo menos 0.01 blocos)
        boolean changed = Math.abs(x - lastPlayerX) >= 0.01 ||
                          Math.abs(y - lastPlayerY) >= 0.01 ||
                          Math.abs(z - lastPlayerZ) >= 0.01;
        
        // Atualiza as últimas coordenadas
        lastPlayerX = x;
        lastPlayerY = y;
        lastPlayerZ = z;
        
        return changed;
    }

    /**
     * Centraliza a lógica de otimização para a geração das linhas do Debug HUD (F3).
     *
     * @param client O cliente Minecraft.
     * @param originalLines A lista de linhas gerada pelo método original do DebugHud.
     * @return A lista de linhas a ser exibida (do cache ou a recém-gerada).
     */
    public static List<String> getOptimizedDebugInfo(MinecraftClient client, List<String> originalLines) {
        if (!BariumConfig.ENABLE_HUD_CACHING) {
            return originalLines; // Se o cache estiver desabilitado, sempre retorna o original
        }

        // Obtém o tempo atual do tick do jogo
        long currentTick = client.world != null ? client.world.getTime() : 0;
        
        // Verifica se as coordenadas mudaram significativamente (isso força uma atualização para mudanças comuns)
        boolean coordsChanged = havePlayerCoordinatesChanged(client);
        
        // Verifica se é hora de uma atualização completa com base no intervalo configurado
        boolean timeToUpdate = (currentTick - lastDebugHudUpdateTime) >= BariumConfig.HUD_UPDATE_INTERVAL_TICKS;

        // Determina se devemos atualizar as informações de depuração
        boolean shouldRefresh = coordsChanged || timeToUpdate;

        if (shouldRefresh) {
            // Se devemos atualizar, preenche o cache com as novas linhas e atualiza o tempo da última atualização
            cachedDebugInfoLines = new ArrayList<>(originalLines); // Cria uma nova lista para evitar modificar a original
            lastDebugHudUpdateTime = currentTick;
            BariumMod.LOGGER.debug("Debug HUD atualizado. Coordenadas mudaram: " + coordsChanged + ", Hora de atualizar: " + timeToUpdate);
        } else {
            // Caso contrário, retorna a versão cacheada
            BariumMod.LOGGER.debug("Debug HUD retornou cacheado. Tick atual: " + currentTick + ", Última atualização: " + lastDebugHudUpdateTime);
        }

        return cachedDebugInfoLines;
    }
    
    /**
     * Limpa o cache de conteúdo da HUD.
     * Deve ser chamado quando há mudanças significativas no estado do jogo (e.g., troca de mundo).
     */
    public static void clearHudCache() {
        BariumMod.LOGGER.debug("Limpando cache da HUD.");
        HUD_CONTENT_CACHE.clear();
        UPDATE_COUNTERS.clear();
        cachedDebugInfoLines.clear(); // Limpa o cache específico de informações de depuração
        lastDebugHudUpdateTime = 0;
        lastPlayerX = 0; lastPlayerY = 0; lastPlayerZ = 0; // Reseta as últimas coordenadas
    }
}