// --- Substitua o conteúdo de HandledScreenMixin.java por este ---
package com.barium.client.mixin;

import com.barium.client.util.TooltipManager;
import com.barium.config.BariumConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Item.TooltipContext;
import net.minecraft.item.tooltip.TooltipType; // Import necessário
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
        
        if (!itemStack.isEmpty()) {
            if (TooltipManager.hasCachedTooltip(itemStack)) {
                context.drawTooltip(MinecraftClient.getInstance().textRenderer, TooltipManager.getCachedTooltip(), mouseX, mouseY);
                ci.cancel();
            } else {
                MinecraftClient client = MinecraftClient.getInstance();

                // --- CORREÇÃO DA API ---
                // 1. Crie o TooltipContext correto.
                TooltipContext tooltipContext = client.options.advancedItemTooltips ? TooltipContext.BASIC.withAdvancedDetails() : TooltipContext.BASIC;
                // 2. Crie o TooltipType correto.
                TooltipType tooltipType = TooltipType.BASIC;
                // 3. Chame o método getTooltip com os 3 argumentos na ordem correta.
                List<Text> tooltipLines = itemStack.getTooltip(tooltipContext, client.player, tooltipType);
                
                TooltipManager.cacheTooltip(itemStack, tooltipLines);
            }
        } else {
            TooltipManager.clearCache();
        }
    }
}