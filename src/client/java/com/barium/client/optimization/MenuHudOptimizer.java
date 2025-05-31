package com.barium.client.optimization;

import com.barium.BariumMod;
import com.barium.config.BariumConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.VertexConsumerProvider; // Não é estritamente necessário se usar DrawContext
// import org.joml.Matrix4f; // Não é estritamente necessário se usar DrawContext

/**
 * Otimiza a HUD do menu principal (TitleScreen), controlando o FPS do panorama
 * e potencialmente cacheando elementos estáticos da UI.
 */
public class MenuHudOptimizer {

    private static long lastPanoramaRenderTime = 0; // Tempo do último frame do panorama
    // Tipo da variável permanece Framebuffer
    private static Framebuffer staticUiFramebuffer;
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

        // CORRIGIDO: Usar System.nanoTime() para obter tempo em milissegundos
        long currentTime = System.nanoTime() / 1_000_000L;
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

        // Se a tela mudou de tamanho ou o framebuffer não existe ou não está alocado (getInternalId() == 0), recria
        // CORRIGIDO: Usar getInternalId() para verificar a alocação do FBO
        if (staticUiFramebuffer == null || cachedWidth != screenWidth || cachedHeight != screenHeight || staticUiFramebuffer.getInternalId() == 0) {
            clearCache(); // Limpa o antigo se existir
            // CORRIGIDO FINALMENTE: Usar FramebufferFactory.create()
            staticUiFramebuffer = client.getFramebufferFactory().create(screenWidth, screenHeight, true, MinecraftClient.IS_SYSTEM_MAC);
            staticUiFramebuffer.setClearColor(0.0F, 0.0F, 0.0F, 0.0F); // Definir cor de clear (transparente)
            cachedWidth = screenWidth;
            cachedHeight = screenHeight;
            BariumMod.LOGGER.debug("MenuHudOptimizer: Recriando static UI cache ({}, {})", screenWidth, screenHeight);
        }

        Framebuffer defaultFramebuffer = client.getFramebuffer();

        // 1. Vincula nosso FBO para escrita e limpa.
        staticUiFramebuffer.beginWrite(true);
        
        // 2. O DrawContext para renderizar no nosso FBO.
        // Este construtor de DrawContext (client, framebuffer) configura a projeção corretamente.
        DrawContext fboDrawContext = new DrawContext(client, staticUiFramebuffer);

        // --- Renderiza elementos estáticos para o framebuffer usando o DrawContext do FBO ---
        String versionText = client.getGameVersion();
        fboDrawContext.drawTextWithShadow(versionText, 2, screenHeight - 20, 0xFFFFFF);

        String copyrightText = "Copyright Mojang AB. Do not distribute!";
        fboDrawContext.drawTextWithShadow(copyrightText, screenWidth - client.textRenderer.getWidth(copyrightText) - 2, screenHeight - 10, 0xFFFFFF);
        
        // 3. Restaurar o framebuffer padrão do jogo
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
        context.drawTexture(
            staticUiFramebuffer.getColorAttachment().getGlId(),
            0, 0, // Posição X, Y
            cachedWidth, cachedHeight, // Largura, Altura do destino na tela
            0, 0, // UV X, UV Y do início da textura
            cachedWidth, cachedHeight, // Largura, Altura da região UV (geralmente total do FBO)
            cachedWidth, cachedHeight // Largura e altura da textura real (para scaling)
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
        // CORRIGIDO: Usar getInternalId() para verificar a alocação do FBO
        return BariumConfig.ENABLE_MENU_OPTIMIZATION && BariumConfig.CACHE_MENU_STATIC_UI && staticUiFramebuffer != null && staticUiFramebuffer.getInternalId() != 0;
    }
}