package com.barium.client.util;

import com.barium.client.mixin.FramebufferAccessor;
import com.barium.config.BariumConfig;
import com.mojang.blaze3d.platform.GlStateManager;
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
            
            // CORREÇÃO 1: Adicionado o nome "BariumRetro" no construtor
            retroBuffer = new SimpleFramebuffer("BariumRetro", scaledW, scaledH, true);
            
            FilterMode filter = BariumConfig.C.USE_RETRO_FILTER ? FilterMode.NEAREST : FilterMode.LINEAR;
            retroBuffer.setFilter(filter);
            
            lastWidth = scaledW;
            lastHeight = scaledH;
            lastScale = BariumConfig.C.RENDER_SCALE_PERCENT;
        }

        // CORREÇÃO 2: Removido IS_MACOS/IS_SYSTEM_MAC, usamos false (padrão seguro)
        retroBuffer.setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
        retroBuffer.clear(false); // No Mac seria true, mas false funciona universalmente
        
        // CORREÇÃO 3: Tenta usar beginWrite padrão. Se falhar no futuro, usamos GL direto.
        // A maioria das versões 1.21.x ainda usa beginWrite(boolean setViewport).
        retroBuffer.beginWrite(true);
    }

    public static void endRenderAndBlit(MinecraftClient client) {
        if (!isActive() || retroBuffer == null) return;

        // Volta para o Framebuffer principal (da Janela)
        Framebuffer mainBuffer = client.getFramebuffer();
        mainBuffer.beginWrite(true);

        // CORREÇÃO 4: Substituído draw() por blitToScreen() que é o padrão moderno
        // Isso desenha o conteúdo do retroBuffer na tela inteira
        retroBuffer.blitToScreen();
    }
    
    public static void resize() {
        if (retroBuffer != null) {
            retroBuffer.delete();
            retroBuffer = null;
        }
    }
}