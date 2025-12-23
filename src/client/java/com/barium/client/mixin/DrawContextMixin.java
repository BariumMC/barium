package com.barium.client.mixin;

import com.barium.config.BariumConfig;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.text.OrderedText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DrawContext.class)
public class DrawContextMixin {

    /**
     * Otimização: Evita desenhar retângulos (fill) que são invisíveis (alpha 0) ou sem tamanho.
     * Isso reduz drasticamente chamadas de desenho inúteis em GUIs complexas.
     */
    @Inject(method = "fill(Lnet/minecraft/client/render/RenderLayer;IIIII)V", at = @At("HEAD"), cancellable = true)
    private void barium$cullInvisibleFills(RenderLayer layer, int x1, int y1, int x2, int y2, int color, CallbackInfo ci) {
        if (!BariumConfig.C.ENABLE_GUI_OPTIMIZATION) return;

        // Verifica se a cor é totalmente transparente (Alpha == 0)
        if ((color & 0xFF000000) == 0) {
            ci.cancel();
            return;
        }

        // Verifica se o retângulo tem área zero
        if (x1 == x2 || y1 == y2) {
            ci.cancel();
        }
    }

    /**
     * Otimização: Evita processar desenho de textos vazios.
     */
    @Inject(method = "drawText(Lnet/minecraft/client/font/TextRenderer;Ljava/lang/String;IIIZ)I", at = @At("HEAD"), cancellable = true)
    private void barium$cullEmptyString(TextRenderer textRenderer, String text, int x, int y, int color, boolean shadow, CallbackInfoReturnable<Integer> cir) {
        if (BariumConfig.C.ENABLE_GUI_OPTIMIZATION && (text == null || text.isEmpty())) {
            cir.setReturnValue(0);
        }
    }

    @Inject(method = "drawText(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/Text;IIIZ)I", at = @At("HEAD"), cancellable = true)
    private void barium$cullEmptyText(TextRenderer textRenderer, Text text, int x, int y, int color, boolean shadow, CallbackInfoReturnable<Integer> cir) {
        // StringVisitable.EMPTY é usado internamente para textos vazios
        if (BariumConfig.C.ENABLE_GUI_OPTIMIZATION && (text == null || text.getString().isEmpty())) {
            cir.setReturnValue(0);
        }
    }
}