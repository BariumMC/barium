package com.barium.client.mixin;

import com.barium.client.util.TooltipManager;
import com.barium.config.BariumConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
// The 'Slot' import is no longer needed here
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
            target = "Lnet/minecraft/client/gui/screen/ingame/HandledScreen;renderTooltip(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/item/ItemStack;II)V"
        ),
        cancellable = true,
        locals = LocalCapture.CAPTURE_FAILHARD
    )
    // --- CORREÇÃO: O parâmetro 'Slot slot' foi removido porque não existe como uma variável local para captura. ---
    private void barium$cacheAndRenderTooltip(
        DrawContext context, int mouseX, int mouseY, float delta,
        CallbackInfo ci,
        int i, int j,
        ItemStack itemStack // Apenas o itemStack é capturado como um novo local.
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