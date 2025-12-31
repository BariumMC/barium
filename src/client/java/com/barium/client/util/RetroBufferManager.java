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
            
            // CORREÇÃO 1: Construtor agora usa apenas 3 argumentos (removemos o boolean do Mac)
            retroBuffer = new SimpleFramebuffer(scaledW, scaledH, true);
            
            FilterMode filter = BariumConfig.C.USE_RETRO_FILTER ? FilterMode.NEAREST : FilterMode.LINEAR;
            retroBuffer.setFilter(filter);
            
            lastWidth = scaledW;
            lastHeight = scaledH;
            lastScale = BariumConfig.C.RENDER_SCALE_PERCENT;
        }

        // CORREÇÃO 2: Usa IS_MACOS em vez de IS_SYSTEM_MAC, ou false por segurança
        // (Geralmente 'true' no Mac evita erros de driver, mas false é o padrão seguro)
        retroBuffer.clear(MinecraftClient.IS_MACOS);
        
        // Inicia a escrita no buffer pequeno
        retroBuffer.beginWrite(true);
    }

    public static void endRenderAndBlit(MinecraftClient client) {
        if (!isActive() || retroBuffer == null) return;

        // Volta para o Framebuffer principal (da Janela)
        client.getFramebuffer().beginWrite(true);

        // CORREÇÃO 3: Substituímos o código manual de desenho e RenderSystem
        // pelo novo método "blitToScreen()" que o log de erro sugeriu existir na classe Framebuffer.
        // Ele lida internamente com blend, depth e desenho na tela cheia.
        retroBuffer.blitToScreen();
    }
    
    public static void resize() {
        if (retroBuffer != null) {
            retroBuffer.delete();
            retroBuffer = null;
        }
    }
}