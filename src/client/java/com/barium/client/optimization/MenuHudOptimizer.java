package com.barium.client.optimization;

import com.barium.BariumMod;
import com.barium.config.BariumConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.ManagedFramebuffer; // Importar a classe concreta
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Util;

/**
 * Otimiza a HUD do menu principal (TitleScreen), controlando o FPS do panorama
 * e potencialmente cacheando elementos estáticos da UI.
 */
public class MenuHudOptimizer {

    private static long lastPanoramaRenderTime = 0; // Tempo do último frame do panorama
    // Usar ManagedFramebuffer como tipo concreto para a instância
    private static ManagedFramebuffer staticUiFramebuffer;
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

        // CORRIGIDO: Usar System.nanoTime() ou Util.getMeasuringTimeMs()
        long currentTime = System.nanoTime() / 1_000_000L; // Tempo atual em milissegundos
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
     * @param screenWidth A largura da tela atual.
     * @param screenHeight A altura da tela atual.
     */
    public static void updateStaticUiCache(int screenWidth, int screenHeight) {
        if (!BariumConfig.ENABLE_MENU_OPTIMIZATION || !BariumConfig.CACHE_MENU_STATIC_UI) {
            clearCache();
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();

        // Se a tela mudou de tamanho ou o framebuffer não existe ou não está inicializado, recria
        // CORRIGIDO: isInitialized() em ManagedFramebuffer
        if (staticUiFramebuffer == null || cachedWidth != screenWidth || cachedHeight != screenHeight || !staticUiFramebuffer.isInitialized()) {
            clearCache(); // Limpa o antigo se existir
            // CORRIGIDO: Usar FramebufferFactory para criar ManagedFramebuffer
            staticUiFramebuffer = client.getFramebufferFactory().create(screenWidth, screenHeight, true, MinecraftClient.IS_SYSTEM_MAC);
            staticUiFramebuffer.setClearColor(0.0F, 0.0F, 0.0F, 0.0F); // Definir cor de clear (transparente)
            cachedWidth = screenWidth;
            cachedHeight = screenHeight;
            BariumMod.LOGGER.debug("MenuHudOptimizer: Recriando static UI cache ({}, {})", screenWidth, screenHeight);
        }

        Framebuffer defaultFramebuffer = client.getFramebuffer();

        // 1. Vincula nosso FBO para escrita e limpa.
        // CORRIGIDO: Usar beginWrite(true)
        staticUiFramebuffer.beginWrite(true); // Vincula e limpa cor e profundidade
        
        // 2. Criar uma nova instância de DrawContext para renderizar no nosso FBO.
        // Este construtor de DrawContext (client, framebuffer) é o que configura
        // a projeção corretamente para o FBO alvo.
        DrawContext fboDrawContext = new DrawContext(client, staticUiFramebuffer);

        // --- Renderiza elementos estáticos para o framebuffer usando o DrawContext do FBO ---
        // Renderiza a versão do jogo
        // CORRIGIDO: client.getGameVersion()
        String versionText = client.getGameVersion();
        // CORRIGIDO: Assinatura de drawTextWithShadow no DrawContext
        fboDrawContext.drawTextWithShadow(versionText, 2, screenHeight - 20, 0xFFFFFF);

        // Renderiza o texto de copyright
        String copyrightText = "Copyright Mojang AB. Do not distribute!";
        fboDrawContext.drawTextWithShadow(copyrightText, screenWidth - client.textRenderer.getWidth(copyrightText) - 2, screenHeight - 10, 0xFFFFFF);

        // 3. Restaurar o framebuffer padrão do jogo
        // CORRIGIDO: Usar beginWrite(true) para restaurar e limpar o framebuffer padrão
        defaultFramebuffer.beginWrite(true);

        BariumMod.LOGGER.debug("MenuHudOptimizer: Static UI cache atualizado.");
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

        // Desenha a textura do nosso framebuffer usando seu ID OpenGL
        // CORRIGIDO: getColorAttachment().getGlId() para o ID da textura
        context.drawTexture(
            staticUiFramebuffer.getColorAttachment().getGlId(),
            0, 0, cachedWidth, cachedHeight, // Posição e tamanho na tela
            0, 0, // Coordenadas UV de início (top-left da textura)
            cachedWidth, cachedHeight, // Largura e altura da região UV
            cachedWidth, cachedHeight // Largura e altura totais da textura (para escalonamento)
        );
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
        // CORRIGIDO: isInitialized() em ManagedFramebuffer
        return BariumConfig.ENABLE_MENU_OPTIMIZATION && BariumConfig.CACHE_MENU_STATIC_UI && staticUiFramebuffer != null && staticUiFramebuffer.isInitialized();
    }
}