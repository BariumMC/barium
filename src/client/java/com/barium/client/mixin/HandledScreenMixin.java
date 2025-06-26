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
        // --- CORREÇÃO: O alvo da injeção foi alterado para o método correto. ---
        // A lógica da tooltip não está mais em 'render', mas sim em 'drawMouseoverTooltip'.
        method = "drawMouseoverTooltip(Lnet/minecraft/client/gui/DrawContext;II)V",
        at = @At(
            value = "INVOKE",
            // Este target para a chamada de renderTooltip está correto dentro do novo método.
            target = "Lnet/minecraft/client/gui/screen/ingame/HandledScreen;renderTooltip(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/item/ItemStack;II)V"
        ),
        cancellable = true,
        locals = LocalCapture.CAPTURE_FAILHARD
    )
    // A assinatura do método foi atualizada para corresponder a 'drawMouseoverTooltip', removendo o float 'delta'.
    private void barium$cacheAndRenderTooltip(
        DrawContext context, int mouseX, int mouseY,
        CallbackInfo ci,
        ItemStack itemStack // A captura da variável local 'itemStack' ainda é necessária e correta.
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