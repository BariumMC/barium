package com.barium.client.mixin;

import com.barium.client.util.TooltipManager;
import com.barium.config.BariumConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;

@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin {

    @Inject(
        method = "render(Lnet/minecraft/client/gui/DrawContext;IIF)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/screen/ingame/HandledScreen;renderTooltip(Lnet/minecraft/client/gui/DrawContext;II)V"
        ),
        cancellable = true,
        locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void barium$cacheAndRenderTooltip(
        DrawContext context, int mouseX, int mouseY, float delta,
        CallbackInfo ci,
        int i, int j,
        Slot slot,
        ItemStack itemStack
    ) {
        if (!BariumConfig.C.ENABLE_TOOLTIP_CACHING) return;

        if (itemStack.isEmpty()) {
            TooltipManager.clearCache();
            return;
        }

        if (TooltipManager.hasCachedTooltip(itemStack)) {
            context.drawTooltip(MinecraftClient.getInstance().textRenderer, TooltipManager.getCachedTooltip(), mouseX, mouseY);
            ci.cancel();
        } else {
            MinecraftClient client = MinecraftClient.getInstance();

            // --- CORREÇÃO ---
            // 1. O TooltipContext padrão é agora uma interface estática dentro de Item.
            Item.TooltipContext tooltipContext = Item.TooltipContext.DEFAULT;

            // 2. O TooltipType.Builder foi removido. Use TooltipType.BASIC para tooltips de inventário.
            TooltipType tooltipType = TooltipType.BASIC;

            // 3. Chamamos o método getTooltip com os argumentos corretos.
            List<Text> tooltipLines = itemStack.getTooltip(tooltipContext, client.player, tooltipType);

            TooltipManager.cacheTooltip(itemStack, tooltipLines);
        }
    }
}