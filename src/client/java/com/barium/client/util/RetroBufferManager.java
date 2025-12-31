package com.barium.client.util;

import com.barium.client.mixin.FramebufferAccessor;
import com.barium.config.BariumConfig;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.SimpleFramebuffer;
import org.lwjgl.opengl.GL30;

public class RetroBufferManager {
    
    private static SimpleFramebuffer retroBuffer;
    private static int lastWidth = -1;
    private static int lastHeight = -1;
    private static int lastScale = -1;

    public static boolean isActive() {
        return BariumConfig.C.RENDER_SCALE_PERCENT < 100;
    }

    public static void beginRender(MinecraftClient client) {
        if (!isActive()) return;

        int windowW = client.getWindow().getFramebufferWidth();
        int windowH = client.getWindow().getFramebufferHeight();
        
        // Calcula o tamanho reduzido
        float multiplier = BariumConfig.C.RENDER_SCALE_PERCENT / 100f;
        int scaledW = (int)Math.max(1, windowW * multiplier);
        int scaledH = (int)Math.max(1, windowH * multiplier);

        // Se o tamanho mudou, recria o buffer
        if (retroBuffer == null || scaledW != lastWidth || scaledH != lastHeight || lastScale != BariumConfig.C.RENDER_SCALE_PERCENT) {
            if (retroBuffer != null) retroBuffer.delete();
            
            // Construtor atualizado: (String name, int width, int height, boolean useDepth)
            retroBuffer = new SimpleFramebuffer("BariumRetro", scaledW, scaledH, true);
            
            FilterMode filter = BariumConfig.C.USE_RETRO_FILTER ? FilterMode.NEAREST : FilterMode.LINEAR;
            retroBuffer.setFilter(filter);
            
            lastWidth = scaledW;
            lastHeight = scaledH;
            lastScale = BariumConfig.C.RENDER_SCALE_PERCENT;
        }

        // --- MANIPULAÇÃO MANUAL DO FRAMEBUFFER (Substituindo beginWrite/clear) ---

        // 1. Pega o ID do OpenGL do nosso buffer pequeno
        int fboId = ((FramebufferAccessor)retroBuffer).getFbo();

        // 2. Binda o Framebuffer manualmente
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, fboId);

        // 3. Ajusta o Viewport para o tamanho PEQUENO (Crucial para não distorcer)
        RenderSystem.viewport(0, 0, scaledW, scaledH);

        // 4. Limpa o buffer (Cor e Profundidade)
        RenderSystem.clearColor(0.0F, 0.0F, 0.0F, 0.0F); // Substitui retroBuffer.setClearColor
        RenderSystem.clear(GL30.GL_COLOR_BUFFER_BIT | GL30.GL_DEPTH_BUFFER_BIT, false); // Substitui retroBuffer.clear
    }

    public static void endRenderAndBlit(MinecraftClient client) {
        if (!isActive() || retroBuffer == null) return;

        // --- VOLTA PARA O BUFFER DA TELA ---

        Framebuffer mainBuffer = client.getFramebuffer();
        int mainFboId = ((FramebufferAccessor)mainBuffer).getFbo();
        int windowW = client.getWindow().getFramebufferWidth();
        int windowH = client.getWindow().getFramebufferHeight();

        // 1. Binda o Framebuffer principal (Tela)
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, mainFboId);

        // 2. Restaura o Viewport para o tamanho da JANELA
        RenderSystem.viewport(0, 0, windowW, windowH);

        // 3. Desenha o buffer pequeno esticado na tela
        retroBuffer.blitToScreen();
    }
    
    public static void resize() {
        if (retroBuffer != null) {
            retroBuffer.delete();
            retroBuffer = null;
        }
    }
}