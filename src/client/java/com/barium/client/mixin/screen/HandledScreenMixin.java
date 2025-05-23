// src/client/java/com/barium/client/mixin/screen/HandledScreenMixin.java
package com.barium.client.mixin.screen;

import com.barium.config.BariumConfig;
import com.barium.client.optimization.TooltipCache; // Certifique-se que o import de TooltipData foi corrigido lá
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.item.tooltip.TooltipData; // <--- Import CORRETO
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Optional;

@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin extends Screen {

    @Shadow protected MinecraftClient client;

    protected HandledScreenMixin(Text title) {
        super(title);
    }

    // --- REMOVIDO: O antigo redirect para DrawContext.drawTooltip que causava problemas ---
    // Se você quiser cachear tooltips, terá que fazer em um nível diferente ou com uma abordagem mais complexa.
    // A otimização de gradientes abaixo ainda é válida.

    /**
     * Injeta no método `init()` (chamado quando a tela é inicializada) para limpar o cache de tooltips.
     * Isso evita que tooltips de telas anteriores ou itens antigos fiquem em cache.
     * @param ci CallbackInfo.
     */
    @Inject(method = "init", at = @At("TAIL"))
    private void barium$clearTooltipCacheOnInit(CallbackInfo ci) {
        if (BariumConfig.getInstance().TOOLTIP_INVENTORY_OPTIMIZATIONS.ENABLE_TOOLTIP_CACHING) {
            TooltipCache.clearCache();
        }
    }

    // --- Desabilitar Gradientes de Tooltip ---
    // Redireciona as chamadas para `DrawContext.fillGradient` usadas para o fundo e bordas do tooltip.
    // São geralmente duas chamadas, uma para o fundo principal e outra para a borda.

    @Redirect(method = "renderTooltip(Lnet/minecraft/client/gui/DrawContext;Ljava/util/List;Ljava/util/Optional;II)V",
              at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;fillGradient(IIIIII)V", ordinal = 0))
    private void barium$disableTooltipBackgroundGradient(DrawContext instance, int x1, int y1, int x2, int y2, int colorStart, int colorEnd) {
        if (BariumConfig.getInstance().TOOLTIP_INVENTORY_OPTIMIZATIONS.DISABLE_TOOLTIP_GRADIENTS) {
            instance.fill(x1, y1, x2, y2, colorStart);
        } else {
            instance.fillGradient(x1, y1, x2, y2, colorStart, colorEnd);
        }
    }

    @Redirect(method = "renderTooltip(Lnet/minecraft/client/gui/DrawContext;Ljava/util/List;Ljava/util/Optional;II)V",
              at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;fillGradient(IIIIII)V", ordinal = 1))
    private void barium$disableTooltipBorderGradient(DrawContext instance, int x1, int y1, int x2, int y2, int colorStart, int colorEnd) {
        if (BariumConfig.getInstance().TOOLTIP_INVENTORY_OPTIMIZATIONS.DISABLE_TOOLTIP_GRADIENTS) {
            instance.fill(x1, y1, x2, y2, colorStart);
        } else {
            instance.fillGradient(x1, y1, x2, y2, colorStart, colorEnd);
        }
    }
}