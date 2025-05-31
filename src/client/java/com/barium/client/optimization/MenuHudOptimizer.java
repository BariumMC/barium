package com.barium.client.optimization;

import com.barium.BariumMod;
import com.barium.config.BariumConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.VertexConsumerProvider; // Adicionado
import net.minecraft.util.Util;
import org.joml.Matrix4f; // Adicionado

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

        long currentTime = Util.getNanos() / 1_000_000L; // CORRIGIDO: Util.getNanos() para tempo em milissegundos
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
    public static void updateStaticUiCache(int screenWidth, int screenHeight) { // Removido 'originalContext' como parâmetro, pois DrawContext não é thread-safe e é criado por frame.
        if (!BariumConfig.ENABLE_MENU_OPTIMIZATION || !BariumConfig.CACHE_MENU_STATIC_UI) {
            clearCache();
            return;
        }

        // Se a tela mudou de tamanho ou o framebuffer não existe ou não está inicializado, recria
        if (staticUiFramebuffer == null || cachedWidth != screenWidth || cachedHeight != screenHeight || !staticUiFramebuffer.isInitialized()) { // CORRIGIDO: .isInitialized()
            clearCache(); // Limpa o antigo se existir
            staticUiFramebuffer = new Framebuffer(screenWidth, screenHeight, true, MinecraftClient.IS_SYSTEM_MAC); // Framebuffer é uma classe concreta e instanciável
            // SetClearColor é feito no método clear.
            cachedWidth = screenWidth;
            cachedHeight = screenHeight;
            BariumMod.LOGGER.debug("MenuHudOptimizer: Recriando static UI cache ({}, {})", screenWidth, screenHeight);
        }

        MinecraftClient client = MinecraftClient.getInstance();
        Framebuffer defaultFramebuffer = client.getFramebuffer();

        // 1. Vincula nosso FBO para escrita e limpa.
        staticUiFramebuffer.bindWrite(true); // Limpa cor e profundidade
        
        // 2. Configura a matriz de projeção para renderização 2D
        Matrix4f matrix4f = new Matrix4f().setOrtho(0.0F, (float)screenWidth, (float)screenHeight, 0.0F, 1000.0F, 3000.0F); // Matching DrawContext's matrix
        client.gameRenderer.getShader().getProjectionMatrix().set(matrix4f); // Garante que o shader usa a projeção 2D
        
        // 3. Obtém o VertexConsumerProvider para desenhar texto
        VertexConsumerProvider.Immediate immediate = client.getBufferBuilders().getEntityVertexConsumers();

        // --- Renderiza elementos estáticos para o framebuffer ---
        // Renderiza a versão do jogo
        String versionText = client.getGameVersion(); // CORRIGIDO: client.getGameVersion()
        client.textRenderer.drawWithShadow(immediate, versionText, 2, screenHeight - 20, 0xFFFFFF);

        // Renderiza o texto de copyright
        String copyrightText = "Copyright Mojang AB. Do not distribute!";
        client.textRenderer.drawWithShadow(immediate, copyrightText, screenWidth - client.textRenderer.getWidth(copyrightText) - 2, screenHeight - 10, 0xFFFFFF);

        // Termina a renderização para o immediate
        immediate.draw();

        // 4. Restaura o framebuffer padrão do jogo
        defaultFramebuffer.bindWrite(true); // Restaura o framebuffer padrão e o limpa
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
        context.drawTexture(staticUiFramebuffer.getColorAttachment(), 0, 0, cachedWidth, cachedHeight, 0, 0, 1, 1, cachedWidth, cachedHeight); // CORRIGIDO: .getColorAttachment()
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
        return BariumConfig.ENABLE_MENU_OPTIMIZATION && BariumConfig.CACHE_MENU_STATIC_UI && staticUiFramebuffer != null && staticUiFramebuffer.isInitialized(); // CORRIGIDO: .isInitialized()
    }
}