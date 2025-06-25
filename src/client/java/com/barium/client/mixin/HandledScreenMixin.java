package com.barium.client.mixin;

import com.barium.client.util.TooltipManager;
import com.barium.config.BariumConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.Item; // Import necessário para acessar o TooltipContext
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipContext;
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

            // --- CORREÇÃO FINAL E SIMPLIFICADA DA API DE TOOLTIP ---
            
            // 1. O TooltipContext padrão é agora uma interface estática dentro de Item.
            // A verificação de 'advanced tooltips' é feita pelo próprio método.
            Item.TooltipContext tooltipContext = Item.TooltipContext.DEFAULT;
            
            // 2. O TooltipType descreve o contexto da interação.
            TooltipType tooltipType = new TooltipType.Builder().build();

            // 3. Chamamos o método getTooltip com os argumentos corretos.
            List<Text> tooltipLines = itemStack.getTooltip(tooltipContext, client.player, tooltipType);
            
            TooltipManager.cacheTooltip(itemStack, tooltipLines);
        }
    }
}