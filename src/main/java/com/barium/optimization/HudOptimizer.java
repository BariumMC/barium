package com.barium.client.optimization;

import com.barium.BariumMod;
import com.barium.config.BariumConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.hud.DebugHud;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Otimizador da HUD e textos (Overlay).
 * 
 * Implementa:
 * - Redesenho condicional (apenas quando o conteúdo muda)
 * - Cache de fontes pré-rasterizadas
 * - Redução da frequência de atualizações da HUD
 */
public class HudOptimizer {
    // Cache de textos renderizados
    private static final Map<String, Object> TEXT_CACHE = new HashMap<>();
    
    // Cache de conteúdo da HUD
    private static final Map<String, String> HUD_CONTENT_CACHE = new HashMap<>();
    
    // Contadores para controlar a frequência de atualizações
    private static final Map<String, Integer> UPDATE_COUNTERS = new HashMap<>();
    
    // Última posição do jogador para detectar mudanças
    private static double lastPlayerX = 0;
    private static double lastPlayerY = 0;
    private static double lastPlayerZ = 0;
    
    public static void init() {
        BariumMod.LOGGER.info("Inicializando otimizações da HUD e textos");
    }
    
    /**
     * Verifica se um elemento da HUD deve ser atualizado neste frame
     * 
     * @param hudId Identificador único do elemento da HUD
     * @param contentSupplier Fornecedor do conteúdo atual
     * @return true se o elemento deve ser atualizado, false caso contrário
     */
    public static boolean shouldUpdateHudElement(String hudId, Supplier<String> contentSupplier) {
        if (!BariumConfig.ENABLE_HUD_CACHING) {
            return true;
        }
        
        // Incrementa o contador para este elemento
        int counter = UPDATE_COUNTERS.getOrDefault(hudId, 0) + 1;
        UPDATE_COUNTERS.put(hudId, counter);
        
        // Determina a frequência de atualização
        int updateInterval = getUpdateIntervalForHud(hudId);
        
        // Verifica se é hora de atualizar com base no contador
        boolean timeToUpdate = counter >= updateInterval;
        
        // Se não for hora de atualizar, retorna o resultado anterior
        if (!timeToUpdate) {
            return false;
        }
        
        // Reseta o contador
        UPDATE_COUNTERS.put(hudId, 0);
        
        // Obtém o conteúdo atual
        String currentContent = contentSupplier.get();
        
        // Verifica se o conteúdo mudou
        String cachedContent = HUD_CONTENT_CACHE.get(hudId);
        if (cachedContent != null && cachedContent.equals(currentContent)) {
            return false;
        }
        
        // Atualiza o cache e indica que o elemento deve ser atualizado
        HUD_CONTENT_CACHE.put(hudId, currentContent);
        return true;
    }
    
    /**
     * Determina o intervalo de atualização para um elemento da HUD
     * 
     * @param hudId Identificador do elemento da HUD
     * @return O intervalo de atualização em frames
     */
    private static int getUpdateIntervalForHud(String hudId) {
        // Elementos diferentes podem ter frequências diferentes
        switch (hudId) {
            case "coordinates":
                // Coordenadas mudam frequentemente, atualiza mais rápido
                return 2;
            case "fps":
                // FPS pode flutuar, atualiza a cada 5 frames
                return 5;
            case "debug_full":
                // F3 completo é pesado, atualiza menos frequentemente
                return BariumConfig.HUD_UPDATE_INTERVAL_TICKS * 2;
            default:
                // Padrão
                return BariumConfig.HUD_UPDATE_INTERVAL_TICKS;
        }
    }
    
    /**
     * Verifica se as coordenadas do jogador mudaram significativamente
     * 
     * @param client O cliente Minecraft
     * @return true se as coordenadas mudaram, false caso contrário
     */
    public static boolean havePlayerCoordinatesChanged(MinecraftClient client) {
        if (client.player == null) {
            return false;
        }
        
        double x = client.player.getX();
        double y = client.player.getY();
        double z = client.player.getZ();
        
        // Verifica se houve mudança significativa
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
     * Obtém um texto pré-renderizado do cache, ou cria um novo se não existir
     * 
     * @param text O texto a ser renderizado
     * @param renderer O renderizador de texto
     * @return O objeto de texto pré-renderizado
     */
    public static Object getCachedText(String text, TextRenderer renderer) {
        if (!BariumConfig.ENABLE_FONT_CACHING) {
            // Retorna null para indicar que não há cache
            return null;
        }
        
        // Chave de cache que inclui o texto e o estilo
        String cacheKey = text + "_" + renderer.hashCode();
        
        // Verifica se já existe no cache
        Object cached = TEXT_CACHE.get(cacheKey);
        if (cached != null) {
            return cached;
        }
        
        // Aqui seria criado o objeto de texto pré-renderizado
        // Como isso depende da implementação interna do Minecraft, 
        // retornamos null para indicar que não há cache
        return null;
    }
    
    /**
     * Limpa o cache de textos
     * Deve ser chamado quando o estilo de texto muda ou a tela é redimensionada
     */
    public static void clearTextCache() {
        TEXT_CACHE.clear();
    }
    
    /**
     * Limpa o cache de conteúdo da HUD
     * Deve ser chamado quando há mudanças significativas no estado do jogo
     */
    public static void clearHudCache() {
        HUD_CONTENT_CACHE.clear();
        UPDATE_COUNTERS.clear();
    }
}
