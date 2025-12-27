package com.barium.client.optimization;

import com.barium.config.BariumConfig;
import net.minecraft.client.MinecraftClient;
import java.util.List;

public class GuiRendererOptimizer {

    private static int lastDrawCount = 0;
    private static double lastMouseX = -1;
    private static double lastMouseY = -1;
    private static boolean forceRenderNext = true;
    private static int staticFrameCounter = 0;
    
    // Configurações de Throttling
    // Se a GUI estiver estática, renderiza apenas 1 a cada X frames
    private static final int STATIC_GUI_UPDATE_RATE = 3; 

    public static void preRenderOptimize() {
        // Nada pesado aqui
    }

    /**
     * Decide se deve pular a renderização do frame de GUI atual.
     * @param currentDrawCount O tamanho da lista de draws (O(1)).
     * @return true se devemos pular (cancelar) a renderização.
     */
    public static boolean shouldSkipRenderPreparedDraws(int currentDrawCount) {
        if (!BariumConfig.C.ENABLE_GUI_OPTIMIZATION) return false;

        // Sempre renderiza se forçado (ex: redimensionamento, abertura de tela)
        if (forceRenderNext) {
            forceRenderNext = false;
            updateLastState(currentDrawCount);
            return false;
        }

        // Se a quantidade de elementos mudou, renderiza imediatamente.
        if (currentDrawCount != lastDrawCount) {
            updateLastState(currentDrawCount);
            return false;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.mouse == null) return false;

        double mx = client.mouse.getX();
        double my = client.mouse.getY();

        // Se o mouse se moveu, renderiza (para tooltips, hovers, slots).
        // Usamos uma tolerância pequena para evitar jitter de mouse de alta DPI.
        if (Math.abs(mx - lastMouseX) > 0.5 || Math.abs(my - lastMouseY) > 0.5) {
            updateLastState(currentDrawCount);
            lastMouseX = mx;
            lastMouseY = my;
            return false;
        }

        // --- LÓGICA DE GUI ESTÁTICA ---
        // Se chegamos aqui, o mouse está parado e a quantidade de elementos é a mesma.
        // Provavelmente é um inventário aberto sem interação.

        staticFrameCounter++;

        // No modo agressivo, pulamos mais frames quando estático
        int rate = BariumConfig.C.ENABLE_AGGRESSIVE_OPTIMIZATION ? STATIC_GUI_UPDATE_RATE * 2 : STATIC_GUI_UPDATE_RATE;

        // Se ainda não atingimos o limite de frames para pular, CANCELA a renderização.
        if (staticFrameCounter < rate) {
            return true; // PULA! Economiza CPU.
        }

        // Hora de desenhar um frame para atualizar animações (glint, cursor piscando).
        staticFrameCounter = 0;
        return false;
    }

    private static void updateLastState(int count) {
        lastDrawCount = count;
        // Reseta o contador para garantir fluidez imediata após uma interação
        staticFrameCounter = 0;
    }

    public static void postRenderOptimize() {
    }

    public static void reset() {
        forceRenderNext = true;
        lastDrawCount = -1;
        staticFrameCounter = 0;
    }

    public static void forceNextRender() {
        forceRenderNext = true;
    }
}