package com.barium.client.optimization.gui;

import com.barium.BariumMod;
import com.barium.config.BariumConfig;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GuiOptimizer {

    private static final Map<Integer, Object> lastStateHash = new ConcurrentHashMap<>();
    private static Framebuffer guiCacheFramebuffer;
    private static boolean cacheIsValid = false;

    public static void init() {
        BariumMod.LOGGER.info("Inicializando GuiOptimizer");
    }

    // Verifica se devemos redesenhar a GUI ou usar o cache
    public static boolean shouldUseCache(Screen screen, Object currentStateHash) {
        if (!BariumConfig.ENABLE_GUI_OPTIMIZATION) {
            return false;
        }

        int screenId = screen.hashCode();
        Object cachedState = lastStateHash.get(screenId);

        if (cacheIsValid && currentStateHash.equals(cachedState)) {
            // O estado não mudou e o cache é válido, então podemos usar o cache.
            return true;
        }

        // O estado mudou ou o cache é inválido. Devemos redesenhar.
        lastStateHash.put(screenId, currentStateHash);
        cacheIsValid = false; // Invalida o cache para forçar um redesenho.
        return false;
    }

    // Invalida o cache. Chamado quando a tela é fechada ou redimensionada.
    public static void invalidateCache() {
        cacheIsValid = false;
        lastStateHash.clear();
        if (guiCacheFramebuffer != null) {
            guiCacheFramebuffer.delete();
            guiCacheFramebuffer = null;
        }
    }

    // Prepara o framebuffer para receber o desenho da GUI.
    public static void beginCacheRender() {
        if (!BariumConfig.ENABLE_GUI_OPTIMIZATION) return;

        MinecraftClient client = MinecraftClient.getInstance();
        int width = client.getWindow().getFramebufferWidth();
        int height = client.getWindow().getFramebufferHeight();

        // Cria ou recria o framebuffer se necessário.
        if (guiCacheFramebuffer == null || guiCacheFramebuffer.textureWidth != width || guiCacheFramebuffer.textureHeight != height) {
            if (guiCacheFramebuffer != null) {
                guiCacheFramebuffer.delete();
            }
            // O true final significa que ele terá um buffer de profundidade.
            guiCacheFramebuffer = new SimpleFramebuffer(width, height, true, MinecraftClient.IS_SYSTEM_MAC);
        }

        // Redireciona o desenho para o nosso framebuffer.
        guiCacheFramebuffer.beginWrite(true);
    }

    // Finaliza a captura do desenho da GUI.
    public static void endCacheRender() {
        if (!BariumConfig.ENABLE_GUI_OPTIMIZATION || guiCacheFramebuffer == null) return;

        // Retorna o desenho para a tela principal.
        MinecraftClient.getInstance().getFramebuffer().beginWrite(true);
        cacheIsValid = true; // O cache agora contém a imagem mais recente.
    }

    // Desenha o conteúdo do nosso framebuffer cacheado diretamente na tela.
    public static void drawCachedGui() {
        if (!BariumConfig.ENABLE_GUI_OPTIMIZATION || guiCacheFramebuffer == null || !cacheIsValid) return;

        // Desenha a textura do nosso framebuffer na tela inteira.
        RenderSystem.disableDepthTest();
        guiCacheFramebuffer.draw(guiCacheFramebuffer.textureWidth, guiCacheFramebuffer.textureHeight);
        RenderSystem.enableDepthTest();
    }
}