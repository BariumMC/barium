package com.barium.client.mixin;

import com.barium.client.util.TooltipManager;
import com.barium.config.BariumConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Item.TooltipContext; // Import corrigido
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;

@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin {

    // Usamos @Shadow para obter acesso ao método que encontra o slot sob o mouse.
    @Shadow protected abstract Slot getSlotAt(double x, double y);

    // E para o handler da tela, para verificar se o slot é válido.
    @Shadow protected abstract boolean isPointOverSlot(Slot slot, double x, double y);

    /**
     * Injeta no método de renderização principal da tela.
     * Usamos LocalCapture para pegar a pilha de itens (itemStack) do slot sob o mouse.
     */
    @Inject(
        method = "render(Lnet/minecraft/client/gui/DrawContext;IIF)V",
        at = @At(
            value = "INVOKE",
            // Injetamos antes de o jogo tentar renderizar a tooltip.
            target = "Lnet/minecraft/client/gui/screen/ingame/HandledScreen;renderTooltip(Lnet/minecraft/client/gui/DrawContext;II)V"
        ),
        cancellable = true,
        locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void barium$cacheAndRenderTooltip(
        // Parâmetros do método original
        DrawContext context, int mouseX, int mouseY, float delta,
        // Parâmetros da injeção
        CallbackInfo ci,
        // Variáveis locais capturadas
        int i, int j,
        Slot slot,
        ItemStack itemStack // Esta é a variável que nos interessa!
    ) {
        if (!BariumConfig.C.ENABLE_TOOLTIP_CACHING) return;
        
        // Se a pilha de itens não estiver vazia
        if (!itemStack.isEmpty()) {
            // Verifica se temos um cache válido para este item
            if (TooltipManager.hasCachedTooltip(itemStack)) {
                // Se sim, desenha a tooltip em cache e cancela a renderização original.
                context.drawTooltip(MinecraftClient.getInstance().textRenderer, TooltipManager.getCachedTooltip(), mouseX, mouseY);
                ci.cancel();
            } else {
                // Se não, gera a tooltip, armazena em cache e deixa o jogo renderizá-la normalmente.
                MinecraftClient client = MinecraftClient.getInstance();
                List<Text> tooltipLines = itemStack.getTooltip(client.player, client.options.advancedItemTooltips ? TooltipContext.Default.ADVANCED : TooltipContext.Default.BASIC);
                TooltipManager.cacheTooltip(itemStack, tooltipLines);
            }
        } else {
            // Se o mouse não está sobre um item, limpa o cache.
            TooltipManager.clearCache();
        }
    }
}