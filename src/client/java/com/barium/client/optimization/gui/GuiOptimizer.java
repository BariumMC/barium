package com.barium.client.optimization.gui;

import com.barium.BariumMod;
import com.barium.config.BariumConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.SimpleFramebuffer;
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

    public static boolean shouldUseCache(Screen screen, Object currentStateHash) {
        if (!BariumConfig.ENABLE_GUI_OPTIMIZATION) return false;
        int screenId = screen.hashCode();
        Object cachedState = lastStateHash.get(screenId);
        if (cacheIsValid && currentStateHash.equals(cachedState)) {
            return true;
        }
        lastStateHash.put(screenId, currentStateHash);
        cacheIsValid = false;
        return false;
    }

    public static void invalidateCache() {
        cacheIsValid = false;
        lastStateHash.clear();
        if (guiCacheFramebuffer != null) {
            guiCacheFramebuffer.delete();
            guiCacheFramebuffer = null;
        }
    }

    public static Framebuffer getFramebuffer() {
        return guiCacheFramebuffer;
    }

    public static void beginCacheRender() {
        if (!BariumConfig.ENABLE_GUI_OPTIMIZATION) return;
        MinecraftClient client = MinecraftClient.getInstance();
        int width = client.getWindow().getFramebufferWidth();
        int height = client.getWindow().getFramebufferHeight();

        if (guiCacheFramebuffer == null || guiCacheFramebuffer.textureWidth != width || guiCacheFramebuffer.textureHeight != height) {
            invalidateCache();
            // CORREÇÃO: O construtor correto tem 3 parâmetros.
            guiCacheFramebuffer = new SimpleFramebuffer(width, height, true);
        }

        // CORREÇÃO: O método agora é bind(), não beginWrite().
        guiCacheFramebuffer.bind(true);
        // Limpa a tela do framebuffer com transparência.
        guiCacheFramebuffer.clear(MinecraftClient.IS_SYSTEM_MAC);
    }

    public static void endCacheRender() {
        if (!BariumConfig.ENABLE_GUI_OPTIMIZATION || guiCacheFramebuffer == null) return;
        // Retorna o desenho para a tela principal.
        MinecraftClient.getInstance().getFramebuffer().bind(true);
        cacheIsValid = true;
    }
}