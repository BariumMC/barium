package com.barium.client.optimization;

import com.barium.BariumMod;
import com.barium.config.BariumConfig;
import net.minecraft.client.MinecraftClient;
import java.util.List;

/**
 * Otimizador inteligente para GuiRenderer.
 * Substitui a comparação lenta de listas por heurísticas de Input e Estado.
 */
public class GuiRendererOptimizer {

    private static int lastDrawCount = 0;
    private static double lastMouseX = -1;
    private static double lastMouseY = -1;
    private static boolean forceRenderNext = true;
    private static int staticFrameCounter = 0;

    /**
     * Reseta o estado quando a tela muda ou o frame inicia.
     */
    public static void preRenderOptimize() {
        // Não precisamos fazer nada pesado aqui para evitar overhead.
    }

    /**
     * Decide se deve pular a renderização com base na atividade do usuário e estado da GUI.
     * Retorna TRUE para PULAR a renderização.
     */
    public static boolean shouldSkipRenderPreparedDraws(int currentDrawCount, List<?> currentDraws) {
        if (!BariumConfig.C.ENABLE_GUI_OPTIMIZATION) return false;

        // Se forçado (ex: redimensionamento de tela), renderiza.
        if (forceRenderNext) {
            forceRenderNext = false;
            updateLastState(currentDrawCount);
            return false;
        }

        // Se a contagem de desenhos mudou (ex: abriu um tooltip, item novo apareceu), renderiza.
        if (currentDrawCount != lastDrawCount) {
            updateLastState(currentDrawCount);
            return false;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.mouse == null) return false; // Segurança

        double mx = client.mouse.getX();
        double my = client.mouse.getY();

        // Se o mouse se moveu significativamente, renderiza (para hover states, tooltips, etc).
        if (Math.abs(mx - lastMouseX) > 0.5 || Math.abs(my - lastMouseY) > 0.5) {
            updateLastState(currentDrawCount);
            lastMouseX = mx;
            lastMouseY = my;
            return false;
        }

        // Se chegamos aqui, o mouse está parado e a quantidade de elementos é a mesma.
        // Provavelmente é uma cena estática.
        
        // Aumenta contador de frames estáticos
        staticFrameCounter++;

        // Renderiza a cada 10 frames mesmo estando parado para atualizar animações (ex: cursor piscando, itens girando)
        // Isso reduz a carga da GPU em 90% quando o jogador está parado no inventário.
        if (staticFrameCounter > 10) {
            staticFrameCounter = 0;
            return false; // Renderiza este frame para atualizar animações
        }

        // PULA A RENDERIZAÇÃO
        return true;
    }

    private static void updateLastState(int count) {
        lastDrawCount = count;
        staticFrameCounter = 0;
    }

    /**
     * Chamado após o render para debug.
     */
    public static void postRenderOptimize() {
        // Métricas removidas para produção para economizar CPU
    }

    /**
     * Reseta completamente o otimizador.
     */
    public static void reset() {
        forceRenderNext = true;
        lastDrawCount = -1;
        staticFrameCounter = 0;
    }

    public static void forceNextRender() {
        forceRenderNext = true;
    }
}