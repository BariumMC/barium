package com.barium.client.mixin;

import com.barium.client.util.TooltipManager;
import com.barium.config.BariumConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin<T extends ScreenHandler> {

    @Shadow @Nullable protected Slot focusedSlot;
    @Shadow protected T handler;

    @Unique
    private long barium_screenOpenedAt = 0;

    @Inject(method = "init()V", at = @At("TAIL"))
    private void barium$markOpenTime(CallbackInfo ci) {
        barium_screenOpenedAt = System.currentTimeMillis();
    }

    @Inject(method = "render(Lnet/minecraft/client/gui/DrawContext;IIF)V", at = @At("HEAD"), cancellable = true)
    private void barium$skipFirstRenderFrame(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        long now = System.currentTimeMillis();
        if (now - barium_screenOpenedAt < 16) { // Reduzido para 16ms para evitar shutter
            ci.cancel(); // Pula o primeiro frame de renderização pesada
        }
    }

    @Inject(
        method = "drawMouseoverTooltip(Lnet/minecraft/client/gui/DrawContext;II)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void barium$cacheAndRenderTooltip(DrawContext context, int mouseX, int mouseY, CallbackInfo ci) {
        if (!BariumConfig.C.ENABLE_TOOLTIP_CACHING) return;

        if (this.handler.getCursorStack().isEmpty() && this.focusedSlot != null && this.focusedSlot.hasStack()) {
            final ItemStack itemStack = this.focusedSlot.getStack();

            // Se temos uma tooltip em cache, desenhamos e cancelamos o método original.
            if (TooltipManager.hasCachedTooltip(itemStack)) {
                context.drawTooltip(MinecraftClient.getInstance().textRenderer, TooltipManager.getCachedTooltip(), mouseX, mouseY);
                ci.cancel();
            } else {
                // Se não, deixamos o método original rodar para desenhar, mas antes
                // nós geramos e guardamos a tooltip para a próxima vez.
                MinecraftClient client = MinecraftClient.getInstance();
                Item.TooltipContext tooltipContext = Item.TooltipContext.DEFAULT;
                TooltipType tooltipType = client.options.advancedItemTooltips ? TooltipType.ADVANCED : TooltipType.BASIC;
                List<Text> tooltipLines = itemStack.getTooltip(tooltipContext, client.player, tooltipType);

                TooltipManager.cacheTooltip(itemStack, tooltipLines);
            }
        } else {
            // Se o mouse não está sobre um item, limpamos o cache.
            TooltipManager.clearCache();
        }
    }
}