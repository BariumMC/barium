package com.barium.client.mixin.gui;

import com.barium.client.optimization.gui.GuiOptimizer;
import com.barium.config.BariumConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;

@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin extends Screen {

    @Shadow @Nullable protected Slot focusedSlot;

    protected HandledScreenMixin(Text title) {
        super(title);
    }

    @Inject(
        method = "drawMouseoverTooltip",
        at = @At("HEAD"),
        cancellable = true
    )
    private void barium$useCachedTooltip(DrawContext context, int x, int y, CallbackInfo ci) {
        if (BariumConfig.ENABLE_GUI_OPTIMIZATION) {
            // Se o cache for válido, desenha a tooltip em cache e cancela o método original.
            if (GuiOptimizer.shouldUseCachedTooltip((HandledScreen)(Object)this, this.focusedSlot)) {
                List<Text> tooltip = GuiOptimizer.getCachedTooltip();
                if (tooltip != null && !tooltip.isEmpty()) {
                    context.drawTooltip(this.client.textRenderer, tooltip, x, y);
                    ci.cancel(); // Evita o recálculo caro da tooltip.
                }
            }
        }
    }

    @Inject(
        method = "drawMouseoverTooltip",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/DrawContext;drawTooltip(Lnet/minecraft/client/font/TextRenderer;Ljava/util/List;II)V"
        ),
        locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void barium$cacheNewTooltip(DrawContext context, int x, int y, CallbackInfo ci, ItemStack stack, List<Text> tooltip) {
        // Após a tooltip original ser calculada, a capturamos e a colocamos no cache.
        if (BariumConfig.ENABLE_GUI_OPTIMIZATION && this.focusedSlot != null) {
            GuiOptimizer.cacheTooltip(this.focusedSlot, tooltip);
        }
    }

    // Limpa o cache quando a tela é fechada para evitar memory leaks ou tooltips fantasmas.
    @Inject(method = "close", at = @At("HEAD"))
    private void barium$onClose(CallbackInfo ci) {
        GuiOptimizer.clearTooltipCache();
    }
}