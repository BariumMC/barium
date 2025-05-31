package com.barium.client.optimization;

import com.barium.BariumMod;
import com.barium.config.BariumConfig;
import com.mojang.blaze3d.systems.RenderSystem; // Importar para chamadas OpenGL como RenderSystem.clearColor
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.VertexConsumerProvider; // Para desenhar texto no FBO
import net.minecraft.client.util.math.MatrixStack; // Para manipular a pilha de matrizes do RenderSystem
import org.joml.Matrix4f; // Para criar matriz ortográfica

/**
 * Otimiza a HUD do menu principal (TitleScreen), controlando o FPS do panorama
 * e potencialmente cacheando elementos estáticos da UI.
 */
public class MenuHudOptimizer {

    private static long lastPanoramaRenderTime = 0; // Tempo do último frame do panorama
    // Declaração do Framebuffer. Framebuffer é uma classe concreta em 1.21.5 (não é mais abstrata como no passado).
    // Ou seja, 'new Framebuffer(...)` DEVE funcionar.
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

        // Se a tela mudou de tamanho ou o framebuffer não existe ou não está alocado (getGlId() == 0), recria
        // CORRIGIDO: getGlId() para verificar a alocação do FBO
        if (staticUiFramebuffer == null || cachedWidth != screenWidth || cachedHeight != screenHeight || staticUiFramebuffer.getGlId() == 0) {
            clearCache(); // Limpa o antigo se existir
            // CORRIGIDO: new Framebuffer() para instanciar (é concreta em 1.21.5)
            staticUiFramebuffer = new Framebuffer(screenWidth, screenHeight, true, MinecraftClient.IS_SYSTEM_MAC);
            cachedWidth = screenWidth;
            cachedHeight = screenHeight;
            BariumMod.LOGGER.debug("MenuHudOptimizer: Recriando static UI cache ({}, {})", screenWidth, screenHeight);
        }

        Framebuffer defaultFramebuffer = client.getFramebuffer();

        // --- INICIAR RENDERIZAÇÃO NO NOSSO FBO ---
        // 1. Define a cor de clear e vincula nosso FBO para escrita, e limpa.
        RenderSystem.clearColor(0.0F, 0.0F, 0.0F, 0.0F); // Definir cor de clear (transparente)
        staticUiFramebuffer.beginWrite(true); // Vincula e limpa cor e profundidade
        
        // 2. Configura o estado de renderização 2D (matrizes de projeção/modelo)
        // Isso é crucial para que o texto seja desenhado corretamente no FBO.
        MatrixStack matrixStack = RenderSystem.getModelViewStack(); // Pega a pilha de matrizes atual
        matrixStack.push(); // Salva a matriz atual
        matrixStack.loadIdentity(); // Carrega uma matriz identidade
        RenderSystem.applyModelViewMatrix(); // Aplica a nova matriz
        
        // Define a matriz de projeção para o FBO (ortográfica 2D)
        Matrix4f projectionMatrix = new Matrix4f().setOrtho(0.0F, (float)screenWidth, (float)screenHeight, 0.0F, 1000.0F, 3000.0F);
        RenderSystem.setProjectionMatrix(projectionMatrix); // Aplica a matriz de projeção

        // 3. Obtém o VertexConsumerProvider para desenhar texto
        VertexConsumerProvider.Immediate immediate = client.getBufferBuilders().getEntityVertexConsumers();

        // --- Renderiza elementos estáticos para o framebuffer ---
        String versionText = client.getGameVersion();
        // CORRIGIDO: drawWithShadow(VertexConsumerProvider, String, ...)
        client.textRenderer.drawWithShadow(immediate, versionText, 2, screenHeight - 20, 0xFFFFFF);

        String copyrightText = "Copyright Mojang AB. Do not distribute!";
        client.textRenderer.drawWithShadow(immediate, copyrightText, screenWidth - client.textRenderer.getWidth(copyrightText) - 2, screenHeight - 10, 0xFFFFFF);
        
        // Finaliza a renderização para o immediate VertexConsumerProvider
        immediate.draw();

        // --- RESTAURAR ESTADO DE RENDERIZAÇÃO E FBO ---
        matrixStack.pop(); // Restaura a matriz de modelo-visão anterior
        RenderSystem.applyModelViewMatrix(); // Aplica a matriz restaurada

        // Restaurar o framebuffer padrão do jogo
        RenderSystem.clearColor(0.0F, 0.0F, 0.0F, 1.0F); // Opcional: Define a cor de clear do FBO padrão para o padrão
        defaultFramebuffer.beginWrite(true); // Restaura e limpa o framebuffer padrão (cor e profundidade)

        BariumMod.LOGGER.debug("MenuHudOptimizer: Static UI cache atualizado.");
    }

    /**
     * Desenha o conteúdo cacheado de elementos estáticos da UI.
     *
     * @param context O DrawContext atual.
     */
    public static void drawCachedStaticUi(DrawContext context) {
        if (!BariumConfig.ENABLE_MENU_OPTIMIZATION || !BariumConfig.CACHE_MENU_STATIC_UI || staticUiFramebuffer == null || staticUiFramebuffer.getGlId() == 0) {
            return;
        }

        // Desenha a textura do nosso framebuffer usando seu ID OpenGL
        // CORRIGIDO: getColorAttachment().getGlId() - Este é o método correto
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
        // CORRIGIDO: Usar getGlId() para verificar a alocação do FBO
        return BariumConfig.ENABLE_MENU_OPTIMIZATION && BariumConfig.CACHE_MENU_STATIC_UI && staticUiFramebuffer != null && staticUiFramebuffer.getGlId() != 0;
    }
}