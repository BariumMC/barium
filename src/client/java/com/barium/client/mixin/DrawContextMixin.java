package com.barium.client.mixin;

import com.barium.config.BariumConfig;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
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

        if ((color & 0xFF000000) == 0) {
            ci.cancel();
            return;
        }

        if (x1 == x2 || y1 == y2) {
            ci.cancel();
            return;
        }

        int minX = Math.min(x1, x2);
        int maxX = Math.max(x1, x2);
        int minY = Math.min(y1, y2);
        int maxY = Math.max(y1, y2);

        if (isRectCompletelyOffscreen(minX, minY, maxX, maxY)) {
            ci.cancel();
        }
    }

    @Inject(method = "drawText(Lnet/minecraft/client/font/TextRenderer;Ljava/lang/String;IIIZ)V", at = @At("HEAD"), cancellable = true)
    private void barium$cullEmptyString(TextRenderer textRenderer, String text, int x, int y, int color, boolean shadow, CallbackInfo ci) {
        if (BariumConfig.C.ENABLE_GUI_OPTIMIZATION && (text == null || text.isEmpty() || (color & 0xFF000000) == 0)) {
            ci.cancel();
            return;
        }

        if (!BariumConfig.C.ENABLE_GUI_OPTIMIZATION || textRenderer == null || text == null) return;

        if (isTextCompletelyOffscreen(textRenderer, x, y, textRenderer.getWidth(text))) {
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

        if (isTextCompletelyOffscreen(textRenderer, x, y, textRenderer.getWidth(text))) {
            ci.cancel();
        }
    }


    @Inject(method = "drawTextWithShadow(Lnet/minecraft/client/font/TextRenderer;Ljava/lang/String;III)V", at = @At("HEAD"), cancellable = true)
    private void barium$cullShadowString(TextRenderer textRenderer, String text, int x, int y, int color, CallbackInfo ci) {
        if (!BariumConfig.C.ENABLE_GUI_OPTIMIZATION) return;
        if (text == null || text.isEmpty() || (color & 0xFF000000) == 0) {
            ci.cancel();
            return;
        }

        if (textRenderer != null && isTextCompletelyOffscreen(textRenderer, x, y, textRenderer.getWidth(text))) {
            ci.cancel();
        }
    }

    @Inject(method = "drawTextWithShadow(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/Text;III)V", at = @At("HEAD"), cancellable = true)
    private void barium$cullShadowText(TextRenderer textRenderer, Text text, int x, int y, int color, CallbackInfo ci) {
        if (!BariumConfig.C.ENABLE_GUI_OPTIMIZATION) return;
        if (text == null || (color & 0xFF000000) == 0) {
            ci.cancel();
            return;
        }

        if (textRenderer != null && isTextCompletelyOffscreen(textRenderer, x, y, textRenderer.getWidth(text))) {
            ci.cancel();
        }
    }

    private static boolean isTextCompletelyOffscreen(TextRenderer textRenderer, int x, int y, int textWidth) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.getWindow() == null) return false;

        int windowWidth = client.getWindow().getScaledWidth();
        int windowHeight = client.getWindow().getScaledHeight();
        int textHeight = textRenderer.fontHeight;

        return x >= windowWidth || y >= windowHeight || x + textWidth <= 0 || y + textHeight <= 0;
    }

    private static boolean isRectCompletelyOffscreen(int minX, int minY, int maxX, int maxY) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.getWindow() == null) return false;

        int windowWidth = client.getWindow().getScaledWidth();
        int windowHeight = client.getWindow().getScaledHeight();

        return maxX <= 0 || minX >= windowWidth || maxY <= 0 || minY >= windowHeight;
    }
}
