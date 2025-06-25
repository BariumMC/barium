package com.barium.client.mixin;

import com.barium.client.util.TooltipManager;
import com.barium.config.BariumConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;
import java.util.Optional;

@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin {

    private ItemStack lastHoveredStack = ItemStack.EMPTY;

    /**
     * PASSO 1: VERIFICAÇÃO DO CACHE
     * Se tivermos uma tooltip em cache para o item atual, nós a desenhamos e
     * cancelamos a lógica de renderização de tooltip original do jogo.
     */
    @Inject(
        method = "render(Lnet/minecraft/client/gui/DrawContext;IIF)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ingame/HandledScreen;renderTooltip(Lnet/minecraft/client/gui/DrawContext;II)V"),
        cancellable = true,
        locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void barium$useCachedTooltip(
        DrawContext context, int mouseX, int mouseY, float delta,
        CallbackInfo ci,
        int i, int j,
        Slot slot,
        ItemStack itemStack
    ) {
        if (!BariumConfig.C.ENABLE_TOOLTIP_CACHING) return;
        
        // Se o mouse não está sobre um item, limpa o cache.
        if (itemStack.isEmpty()) {
            TooltipManager.clearCache();
            return;
        }

        this.lastHoveredStack = itemStack; // Armazena o item para o Passo 2

        if (TooltipManager.hasCachedTooltip(itemStack)) {
            context.drawTooltip(MinecraftClient.getInstance().textRenderer, TooltipManager.getCachedTooltip(), mouseX, mouseY);
            ci.cancel();
        }
    }

    /**
     * PASSO 2: CRIAÇÃO DO CACHE
     * Redirecionamos a chamada final de desenho da tooltip. Neste ponto, o jogo JÁ GEROU
     * a lista de textos. Nós a interceptamos, salvamos em nosso cache e depois
     * deixamos a chamada de desenho original prosseguir.
     */
    @Redirect(
        method = "renderTooltip(Lnet/minecraft/client/gui/DrawContext;II)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawTooltip(Lnet/minecraft/client/font/TextRenderer;Ljava/util/List;Ljava/util/Optional;II)V")
    )
    private void barium$cacheGeneratedTooltip(DrawContext context, TextRenderer textRenderer, List<Text> lines, Optional<ItemStack> stack, int x, int y) {
        if (BariumConfig.C.ENABLE_TOOLTIP_CACHING && !this.lastHoveredStack.isEmpty()) {
            // A lista 'lines' é a tooltip que o jogo acabou de gerar. Nós a salvamos.
            TooltipManager.cacheTooltip(this.lastHoveredStack, lines);
        }

        // Deixa a chamada original prosseguir com os mesmos argumentos.
        context.drawTooltip(textRenderer, lines, stack, x, y);
    }
}