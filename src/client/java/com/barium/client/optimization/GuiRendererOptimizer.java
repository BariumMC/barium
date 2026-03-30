package com.barium.client.optimization;

import com.barium.config.BariumConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;

public class GuiRendererOptimizer {

    private static int lastDrawCount = 0;
    private static double lastMouseX = -1;
    private static double lastMouseY = -1;
    private static boolean forceRenderNext = true;
    private static Screen lastScreen = null;
    
    public static void preRenderOptimize() {
        // Nada pesado aqui
    }

    /**
     * Decide se deve pular a renderização de draws preparados.
     *
     * Regras conservadoras para evitar regressão visual/flicker:
     * - nunca pula quando há draws para processar;
     * - só pula quando a lista está vazia.
     *
     * @param currentDrawCount O tamanho da lista de draws (O(1)).
     * @return true se devemos pular (cancelar) a renderização.
     */
    public static boolean shouldSkipRenderPreparedDraws(int currentDrawCount) {
        if (!BariumConfig.C.ENABLE_GUI_OPTIMIZATION) return false;

        MinecraftClient client = MinecraftClient.getInstance();
        Screen currentScreen = client.currentScreen;
        if (currentScreen != lastScreen) {
            lastScreen = currentScreen;
            forceRenderNext = true;
        }

        // Caminho rápido: sem draws, não há trabalho útil em renderPreparedDraws.
        if (currentDrawCount == 0) {
            updateLastState(currentDrawCount, client);
            forceRenderNext = false;
            return true;
        }

        // Sempre renderiza se forçado (ex: redimensionamento, abertura de tela)
        if (forceRenderNext) {
            forceRenderNext = false;
            updateLastState(currentDrawCount, client);
            return false;
        }

        // Se a quantidade de elementos mudou, renderiza imediatamente.
        if (currentDrawCount != lastDrawCount) {
            updateLastState(currentDrawCount, client);
            return false;
        }

        if (client.mouse == null) return false;

        double mx = client.mouse.getX();
        double my = client.mouse.getY();

        // Se o mouse se moveu, renderiza (para tooltips, hovers, slots).
        // Usamos uma tolerância pequena para evitar jitter de mouse de alta DPI.
        if (Math.abs(mx - lastMouseX) > 0.5 || Math.abs(my - lastMouseY) > 0.5) {
            updateLastState(currentDrawCount, client);
            lastMouseX = mx;
            lastMouseY = my;
            return false;
        }

        // Evita flicker: não pulamos mais frames inteiros de GUI com conteúdo.
        return false;
    }

    private static void updateLastState(int count, MinecraftClient client) {
        lastDrawCount = count;
        if (client != null && client.mouse != null) {
            lastMouseX = client.mouse.getX();
            lastMouseY = client.mouse.getY();
        }
    }

    public static void postRenderOptimize() {
    }

    public static void reset() {
        forceRenderNext = true;
        lastDrawCount = -1;
        lastScreen = null;
    }

    public static void forceNextRender() {
        forceRenderNext = true;
    }
}
