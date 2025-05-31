package com.barium.client.optimization;

import com.barium.BariumMod;
import com.barium.config.BariumConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Util;

import java.time.Duration; // Importar Duration

/**
 * Otimiza a HUD do menu principal (TitleScreen), controlando o FPS do panorama
 * e potencialmente cacheando elementos estáticos da UI.
 */
public class MenuHudOptimizer {

    private static long lastPanoramaRenderTime = 0; // Tempo do último frame do panorama
    private static Framebuffer staticUiFramebuffer; // Framebuffer para elementos estáticos da UI
    private static int cachedWidth = 0;
    private static int cachedHeight = 0;

    /**
     * Inicializa o otimizador do menu.
     */
    public static void init() {
        BariumMod.LOGGER.info("Inicializando MenuHudOptimizer");
        clearCache(); // Garante que o cache está limpo na inicialização
    }

    /**
     * Verifica se um novo frame do panorama do menu deve ser renderizado.
     * Controla o FPS do panorama.
     *
     * @return true se o panorama deve ser renderizado neste frame, false caso contrário.
     */
    public static boolean shouldRenderPanoramaFrame() {
        if (!BariumConfig.ENABLE_MENU_OPTIMIZATION) {
            return true; // Sempre renderiza se a otimização estiver desativada
        }

        int targetFps = BariumConfig.MENU_PANORAMA_FPS;
        if (targetFps <= 0) {
            return false; // Desativa o panorama completamente
        }

        long currentTime = Util.get  (); // Tempo atual em milissegundos
        long targetFrameTime = 1000L / targetFps; // Tempo mínimo entre frames em milissegundos

        if (currentTime - lastPanoramaRenderTime >= targetFrameTime) {
            lastPanoramaRenderTime = currentTime;
            return true;
        }
        return false;
    }

    /**
     * Prepara o cache para elementos estáticos da UI do menu.
     * Renderiza o logo do Minecraft, versão e copyright para um framebuffer.
     *
     * @param context O DrawContext original para obter o renderizador de texto.
     * @param screenWidth A largura da tela atual.
     * @param screenHeight A altura da tela atual.
     */
    public static void updateStaticUiCache(DrawContext context, int screenWidth, int screenHeight) {
        if (!BariumConfig.ENABLE_MENU_OPTIMIZATION || !BariumConfig.CACHE_MENU_STATIC_UI) {
            clearCache();
            return;
        }

        // Se a tela mudou de tamanho ou o framebuffer não existe, recria
        if (staticUiFramebuffer == null || cachedWidth != screenWidth || cachedHeight != screenHeight) {
            clearCache(); // Limpa o antigo se existir
            staticUiFramebuffer = new Framebuffer(screenWidth, screenHeight, true, MinecraftClient.IS_SYSTEM_MAC);
            cachedWidth = screenWidth;
            cachedHeight = screenHeight;
            BariumMod.LOGGER.debug("MenuHudOptimizer: Recriando static UI cache ({}, {})", screenWidth, screenHeight);
        }

        // Pega o framebuffer atual do jogo
        Framebuffer defaultFramebuffer = MinecraftClient.getInstance().getFramebuffer();

        // Redireciona a renderização para o nosso framebuffer estático
        staticUiFramebuffer.beginWrite(true); // Limpa o framebuffer antes de desenhar
        staticUiFramebuffer.clear(MinecraftClient.IS_SYSTEM_MAC);

        DrawContext fboContext = new DrawContext(MinecraftClient.getInstance(), staticUiFramebuffer);

        // --- Renderiza elementos estáticos para o framebuffer ---
        // Renderiza o logo do Minecraft (normalmente é desenhado no método TitleScreen#render)
        // O mixin em TitleScreen terá que interceptar a chamada ao DrawContext para o logo.
        // Por simplicidade, vamos apenas cachear o texto de versão e copyright por enquanto.
        // Cachear o logo é mais complexo, pois envolve um método de renderização específico.

        // Renderiza a versão do jogo
        String versionText = MinecraftClient.getInstance().getVersion().getGameVersion();
        fboContext.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, versionText, 2, screenHeight - 20, 0xFFFFFF);

        // Renderiza o texto de copyright
        String copyrightText = "Copyright Mojang AB. Do not distribute!";
        fboContext.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, copyrightText, screenWidth - MinecraftClient.getInstance().textRenderer.getWidth(copyrightText) - 2, screenHeight - 10, 0xFFFFFF);

        // Restaura o framebuffer padrão do jogo
        defaultFramebuffer.beginWrite(false);
        // BariumMod.LOGGER.debug("MenuHudOptimizer: Static UI cache atualizado.");
    }

    /**
     * Desenha o conteúdo cacheado de elementos estáticos da UI.
     *
     * @param context O DrawContext atual.
     */
    public static void drawCachedStaticUi(DrawContext context) {
        if (!BariumConfig.ENABLE_MENU_OPTIMIZATION || !BariumConfig.CACHE_MENU_STATIC_UI || staticUiFramebuffer == null) {
            return;
        }

        // Desenha a textura do nosso framebuffer na tela
        context.drawTexture(staticUiFramebuffer.get               (), 0, 0, cachedWidth, cachedHeight, 0, 0, 1, 1, cachedWidth, cachedHeight);
        // BariumMod.LOGGER.debug("MenuHudOptimizer: Desenhando static UI do cache.");
    }

    /**
     * Limpa o framebuffer de cache e redefine as dimensões.
     */
    public static void clearCache() {
        if (staticUiFramebuffer != null) {
            staticUiFramebuffer.delete();
            staticUiFramebuffer = null;
        }
        cachedWidth = 0;
        cachedHeight = 0;
        // BariumMod.LOGGER.debug("MenuHudOptimizer: Cache limpo.");
    }

    /**
     * Retorna se a otimização de cache de UI estática está ativa e o cache é válido.
     * @return true se o cache é válido e deve ser usado.
     */
    public static boolean isStaticUiCacheValid() {
        return BariumConfig.ENABLE_MENU_OPTIMIZATION && BariumConfig.CACHE_MENU_STATIC_UI && staticUiFramebuffer != null && staticUiFramebuffer.is                   ();
    }
}