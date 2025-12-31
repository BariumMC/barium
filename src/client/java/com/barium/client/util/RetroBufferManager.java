package com.barium.client.util;

import com.barium.config.BariumConfig;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.SimpleFramebuffer;

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
            
            retroBuffer = new SimpleFramebuffer(scaledW, scaledH, true, MinecraftClient.IS_SYSTEM_MAC);
            
            // Aplica o filtro Pixelado (Nearest) ou Suave (Linear)
            FilterMode filter = BariumConfig.C.USE_RETRO_FILTER ? FilterMode.NEAREST : FilterMode.LINEAR;
            retroBuffer.setFilter(filter);
            
            lastWidth = scaledW;
            lastHeight = scaledH;
            lastScale = BariumConfig.C.RENDER_SCALE_PERCENT;
        }

        // Limpa e prepara nosso buffer pequeno
        retroBuffer.clear(MinecraftClient.IS_SYSTEM_MAC);
        retroBuffer.beginWrite(true);
    }

    public static void endRenderAndBlit(MinecraftClient client) {
        if (!isActive() || retroBuffer == null) return;

        // Volta para o Framebuffer principal (da Janela)
        client.getFramebuffer().beginWrite(true);

        // Configura o sistema de renderização para desenhar a imagem esticada
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        
        // Desenha o buffer pequeno esticado para cobrir a tela inteira
        retroBuffer.draw(client.getWindow().getFramebufferWidth(), client.getWindow().getFramebufferHeight());
        
        RenderSystem.enableDepthTest();
    }
    
    // Chamado ao redimensionar a janela para forçar recriação
    public static void resize() {
        if (retroBuffer != null) {
            retroBuffer.delete();
            retroBuffer = null;
        }
    }
}