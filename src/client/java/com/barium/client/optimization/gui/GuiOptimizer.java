package com.barium.client.optimization.gui;

import com.barium.BariumMod;
import com.barium.config.BariumConfig;
import net.minecraft.client.gui.screen.Screen; // Exemplo para Screen
import net.minecraft.client.gui.widget.AbstractWidget; // Exemplo para AbstractWidget

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * Otimiza a renderização de elementos da GUI e telas, reduzindo recálculos desnecessários
 * e redesenhos repetitivos.
 */
public class GuiOptimizer {

    // Mapas para armazenar o último estado e o timestamp da última atualização/renderização
    // A chave pode ser uma combinação do tipo de elemento e seu hash/ID único.
    private static final Map<Integer, Long> lastRenderTimestamp = new ConcurrentHashMap<>();
    private static final Map<Integer, Object> lastStateHash = new ConcurrentHashMap<>();

    /**
     * Inicializa o GuiOptimizer.
     */
    public static void init() {
        BariumMod.LOGGER.info("Inicializando GuiOptimizer");
        clearCache();
    }

    /**
     * Verifica se um elemento da GUI deve ser redesenhado ou ter seu layout recalculado.
     * @param elementId Um ID único para o elemento (e.g., hashCode do objeto, ou uma combinação de ID e tipo).
     * @param currentStateHash Um hash/representação do estado atual do elemento.
     * @return true se o elemento precisar de atualização, false caso contrário.
     */
    public static boolean shouldUpdateGuiElement(Object element, Object currentStateHash) {
        if (!BariumConfig.ENABLE_GUI_OPTIMIZATION) {
            return true; // Se a otimização estiver desativada, sempre permite atualização
        }

        int elementId = element.hashCode(); // Usa o hashCode do objeto como ID simples

        long currentTime = System.currentTimeMillis();
        long lastUpdate = GuiOptimizer.lastRenderTimestamp.getOrDefault(elementId, 0L);
        Object cachedState = GuiOptimizer.lastStateHash.get(elementId);

        // Verifica o intervalo de tempo mínimo entre atualizações
        if ((currentTime - lastUpdate) < BariumConfig.GUI_UPDATE_INTERVAL_MS) {
            // Se o tempo mínimo não passou E o estado não mudou, não atualiza
            if (currentStateHash.equals(cachedState)) {
                return false;
            }
        }

        // Atualiza o cache com o novo estado e timestamp
        GuiOptimizer.lastRenderTimestamp.put(elementId, currentTime);
        GuiOptimizer.lastStateHash.put(elementId, currentStateHash);

        // Se o estado mudou ou o tempo mínimo passou, permite a atualização
        return !currentStateHash.equals(cachedState);
    }

    /**
     * Limpa o cache do otimizador da GUI.
     * Deve ser chamado ao abrir ou fechar telas importantes, ou mudar de mundo.
     */
    public static void clearCache() {
        lastRenderTimestamp.clear();
        lastStateHash.clear();
        BariumMod.LOGGER.info("Cache do GuiOptimizer limpo.");
    }
}
