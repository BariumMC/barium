package com.barium.client.optimization;

import com.barium.BariumMod;
import com.barium.config.BariumConfig;
import net.minecraft.client.gui.render.GuiRenderer;
import java.util.List;

/**
 * Otimizador agressivo para GuiRenderer, inspirado em otimizações do Sodium.
 * Foca em reduzir drasticamente o overhead de renderização de GUI.
 */
public class GuiRendererOptimizer {

    private static int lastDrawCount = 0;
    private static long lastRenderTime = 0;
    private static int frameCounter = 0;
    private static boolean forceRenderNext = false;
    private static List<?> lastDrawsSnapshot = null;

    /**
     * Otimização agressiva: Pré-processamento com cache inteligente.
     */
    public static void preRenderOptimize() {
        if (!BariumConfig.C.ENABLE_GUI_OPTIMIZATION) return;

        frameCounter++;

        // Força render a cada 5 frames para evitar stale GUI
        if (frameCounter % 5 == 0) {
            forceRenderNext = true;
        }
    }

    /**
     * Otimização agressiva: Verifica se devemos pular renderização.
     * Mais inteligente que apenas checar vazio.
     */
    public static boolean shouldSkipRenderPreparedDraws(int drawCount, List<?> currentDraws) {
        if (!BariumConfig.C.ENABLE_GUI_OPTIMIZATION) return false;

        // Sempre render se forçado
        if (forceRenderNext) {
            forceRenderNext = false;
            lastDrawsSnapshot = List.copyOf(currentDraws);
            return false;
        }

        // Se não há draws, sempre pular
        if (drawCount == 0) {
            BariumMod.LOGGER.debug("Pulando renderização de GUI: nenhum draw.");
            return true;
        }

        // Otimização agressiva: se os draws são idênticos aos do último frame renderizado,
        // pula para economizar GPU (assume GUI estática)
        if (lastDrawsSnapshot != null && drawCount == lastDrawsSnapshot.size()) {
            boolean identical = true;
            for (int i = 0; i < drawCount; i++) {
                if (!currentDraws.get(i).equals(lastDrawsSnapshot.get(i))) {
                    identical = false;
                    break;
                }
            }
            if (identical) {
                BariumMod.LOGGER.debug("Pulando renderização de GUI: draws idênticos ao frame anterior.");
                return true;
            }
        }

        // Atualiza snapshot
        lastDrawsSnapshot = List.copyOf(currentDraws);
        return false;
    }

    /**
     * Pós-otimização com métricas agressivas.
     */
    public static void postRenderOptimize() {
        if (!BariumConfig.C.ENABLE_GUI_OPTIMIZATION) return;

        long currentTime = System.nanoTime();
        long deltaTime = currentTime - lastRenderTime;

        // Log agressivo: qualquer render acima de 0.5ms é logged
        if (BariumMod.LOGGER.isDebugEnabled() && deltaTime > 500_000) {
            BariumMod.LOGGER.debug("GUI render agressivo levou {}ms com {} draws",
                deltaTime / 1_000_000.0, lastDrawCount);
        }

        lastRenderTime = currentTime;
    }

    /**
     * Reseta estado agressivamente.
     */
    public static void reset() {
        // Limpa cache se necessário para evitar memory leaks
        if (frameCounter % 60 == 0) { // A cada segundo aproximadamente
            lastDrawsSnapshot = null;
        }
    }

    /**
     * Método para forçar render no próximo frame (útil para mudanças de estado).
     */
    public static void forceNextRender() {
        forceRenderNext = true;
    }
}