package com.barium.client.mixin;

import com.barium.client.util.TooltipManager;
import com.barium.config.BariumConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
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
        // Target the correct method where tooltip logic now resides
        method = "drawMouseoverTooltip(Lnet/minecraft/client/gui/DrawContext;II)V",
        at = @At(
            value = "INVOKE",
            // Find the call to renderTooltip inside drawMouseoverTooltip
            target = "Lnet/minecraft/client/gui/screen/ingame/HandledScreen;renderTooltip(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/item/ItemStack;II)V"
        ),
        cancellable = true,
        locals = LocalCapture.CAPTURE_FAILHARD
    )
    // Update the signature to match the target method (no float delta)
    private void barium$cacheAndRenderTooltip(
        DrawContext context, int mouseX, int mouseY,
        CallbackInfo ci,
        ItemStack itemStack // Capture the local ItemStack
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

            Item.TooltipContext tooltipContext = Item.TooltipContext.DEFAULT;
            TooltipType tooltipType = client.options.advancedItemTooltips ? TooltipType.ADVANCED : TooltipType.BASIC;
            List<Text> tooltipLines = itemStack.getTooltip(tooltipContext, client.player, tooltipType);

            TooltipManager.cacheTooltip(itemStack, tooltipLines);
        }
    }
}