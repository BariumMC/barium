package com.barium.client.optimization;

import com.barium.BariumMod;
import com.barium.config.BariumConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.font.TextRenderer; // Adicionado import para TextRenderer
import net.minecraft.util.Util;

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

        long currentTime = Util.getRunTime(); // CORRIGIDO: Util.getRunTime()
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
     * @param originalContext O DrawContext original para obter o renderizador de texto e realizar as chamadas de desenho.
     * @param screenWidth A largura da tela atual.
     * @param screenHeight A altura da tela atual.
     */
    public static void updateStaticUiCache(DrawContext originalContext, int screenWidth, int screenHeight) {
        if (!BariumConfig.ENABLE_MENU_OPTIMIZATION || !BariumConfig.CACHE_MENU_STATIC_UI) {
            clearCache();
            return;
        }

        // Se a tela mudou de tamanho ou o framebuffer não existe ou não está alocado, recria
        if (staticUiFramebuffer == null || cachedWidth != screenWidth || cachedHeight != screenHeight || !staticUiFramebuffer.isAllocated()) { // CORRIGIDO: .isAllocated()
            clearCache(); // Limpa o antigo se existir
            staticUiFramebuffer = new Framebuffer(screenWidth, screenHeight, true, MinecraftClient.IS_SYSTEM_MAC); // Framebuffer é uma classe concreta, o erro anterior era uma cascata ou misconfig
            staticUiFramebuffer.setClearColor(0.0F, 0.0F, 0.0F, 0.0F); // Fundo transparente
            cachedWidth = screenWidth;
            cachedHeight = screenHeight;
            BariumMod.LOGGER.debug("MenuHudOptimizer: Recriando static UI cache ({}, {})", screenWidth, screenHeight);
        }

        // Pega o framebuffer atual do jogo para restaurar depois
        Framebuffer defaultFramebuffer = MinecraftClient.getInstance().getFramebuffer();

        // Redireciona a renderização para o nosso framebuffer estático e o limpa
        staticUiFramebuffer.bindWrite(true); // CORRIGIDO: .bindWrite(true) para vincular e limpar
        // Nao precisa de staticUiFramebuffer.clear() aqui, pois bindWrite(true) ja limpa

        // --- Renderiza elementos estáticos para o framebuffer usando o contexto original ---
        TextRenderer textRenderer = originalContext.getTextRenderer(); // Usa o textRenderer do contexto original

        // Renderiza a versão do jogo
        String versionText = MinecraftClient.getInstance().getGameVersion(); // CORRIGIDO: .getGameVersion()
        originalContext.drawTextWithShadow(textRenderer, versionText, 2, screenHeight - 20, 0xFFFFFF);

        // Renderiza o texto de copyright
        String copyrightText = "Copyright Mojang AB. Do not distribute!";
        originalContext.drawTextWithShadow(textRenderer, copyrightText, screenWidth - textRenderer.getWidth(copyrightText) - 2, screenHeight - 10, 0xFFFFFF);

        // Restaura o framebuffer padrão do jogo
        defaultFramebuffer.bindWrite(true); // CORRIGIDO: .bindWrite(true) para restaurar e limpar o framebuffer padrão
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
        context.drawTexture(staticUiFramebuffer.getTextureId(), 0, 0, cachedWidth, cachedHeight, 0, 0, 1, 1, cachedWidth, cachedHeight); // CORRIGIDO: .getTextureId()
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
        return BariumConfig.ENABLE_MENU_OPTIMIZATION && BariumConfig.CACHE_MENU_STATIC_UI && staticUiFramebuffer != null && staticUiFramebuffer.isAllocated(); // CORRIGIDO: .isAllocated()
    }
}