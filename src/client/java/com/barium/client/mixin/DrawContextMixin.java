package com.barium.client.mixin;

import com.barium.config.BariumConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DrawContext.class)
public class DrawContextMixin {

    @Inject(method = "fill(Lcom/mojang/blaze3d/pipeline/RenderPipeline;IIIII)V", at = @At("HEAD"), cancellable = true)
    private void barium$cullInvisibleFills(RenderPipeline pipeline, int x1, int y1, int x2, int y2, int color, CallbackInfo ci) {
        if (!BariumConfig.C.ENABLE_GUI_OPTIMIZATION) return;

        // Verificação rápida de Alpha: (color >> 24) & 0xFF. Se for 0, é totalmente transparente.
        if ((color & 0xFF000000) == 0) {
            ci.cancel();
            return;
        }

        // Área zero
        if (x1 == x2 || y1 == y2) {
            ci.cancel();
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.getWindow() == null) {
            return;
        }

        int windowWidth = client.getWindow().getScaledWidth();
        int windowHeight = client.getWindow().getScaledHeight();
        int minX = Math.min(x1, x2);
        int maxX = Math.max(x1, x2);
        int minY = Math.min(y1, y2);
        int maxY = Math.max(y1, y2);

        // Retângulo totalmente fora da viewport da GUI
        if (maxX <= 0 || minX >= windowWidth || maxY <= 0 || minY >= windowHeight) {
            ci.cancel();
        }
    }

    @Inject(method = "drawText(Lnet/minecraft/client/font/TextRenderer;Ljava/lang/String;IIIZ)V", at = @At("HEAD"), cancellable = true)
    private void barium$cullEmptyString(TextRenderer textRenderer, String text, int x, int y, int color, boolean shadow, CallbackInfo ci) {
        // Verifica se o texto é nulo, vazio, ou se a cor é transparente
        if (BariumConfig.C.ENABLE_GUI_OPTIMIZATION && (text == null || text.isEmpty() || (color & 0xFF000000) == 0)) {
            ci.cancel();
            return;
        }

        if (!BariumConfig.C.ENABLE_GUI_OPTIMIZATION || textRenderer == null) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.getWindow() == null) return;

        int windowWidth = client.getWindow().getScaledWidth();
        int windowHeight = client.getWindow().getScaledHeight();
        int textWidth = textRenderer.getWidth(text);
        int textHeight = textRenderer.fontHeight;

        if (x >= windowWidth || y >= windowHeight || x + textWidth <= 0 || y + textHeight <= 0) {
            ci.cancel();
        }
    }

    @Inject(method = "drawText(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/Text;IIIZ)V", at = @At("HEAD"), cancellable = true)
    private void barium$cullEmptyText(TextRenderer textRenderer, Text text, int x, int y, int color, boolean shadow, CallbackInfo ci) {
        if (BariumConfig.C.ENABLE_GUI_OPTIMIZATION && (text == null || (color & 0xFF000000) == 0)) {
            ci.cancel();
            return;
        }

        if (!BariumConfig.C.ENABLE_GUI_OPTIMIZATION || textRenderer == null || text == null) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.getWindow() == null) return;

        int windowWidth = client.getWindow().getScaledWidth();
        int windowHeight = client.getWindow().getScaledHeight();
        int textWidth = textRenderer.getWidth(text);
        int textHeight = textRenderer.fontHeight;

        if (x >= windowWidth || y >= windowHeight || x + textWidth <= 0 || y + textHeight <= 0) {
            ci.cancel();
        }
    }
}
