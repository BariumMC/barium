package com.barium.client.mixin;

import com.barium.client.util.TooltipManager;
import com.barium.config.BariumConfig;
import net.minecraft.client.MinecraftClient; // Import corrigido
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.item.TooltipContext; // Import necessário
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin {

    // CORREÇÃO: Usando o nome mapeado correto 'focusedStack'
    @Shadow protected ItemStack focusedStack;

    @Inject(
        method = "render(Lnet/minecraft/client/gui/DrawContext;IIF)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/screen/ingame/HandledScreen;renderTooltip(Lnet/minecraft/client/gui/DrawContext;II)V"
        ),
        cancellable = true
    )
    private void barium$useCachedTooltip(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (!BariumConfig.C.ENABLE_TOOLTIP_CACHING) return;
        
        // CORREÇÃO: Usa o nome de campo correto
        if (TooltipManager.hasCachedTooltip(this.focusedStack)) {
            context.drawTooltip(MinecraftClient.getInstance().textRenderer, TooltipManager.getCachedTooltip(), mouseX, mouseY);
            ci.cancel();
        } else {
            TooltipManager.clearCache();
        }
    }

    /**
     * Injeta DEPOIS que a tooltip é gerada e desenhada.
     * Aqui, nós armazenamos em cache a tooltip que acabou de ser criada.
     */
    @Inject(
        // Este método é o responsável por desenhar a tooltip do item focado.
        method = "drawMouseoverTooltip(Lnet/minecraft/client/gui/DrawContext;II)V",
        // Injetamos no final do método (TAIL).
        at = @At("TAIL")
    )
    private void barium$cacheNewTooltip(DrawContext context, int x, int y, CallbackInfo ci) {
        if (!BariumConfig.C.ENABLE_TOOLTIP_CACHING) return;

        // Se o item focado não estiver vazio, geramos e armazenamos sua tooltip.
        if (!this.focusedStack.isEmpty()) {
            MinecraftClient client = MinecraftClient.getInstance();
            
            // CORREÇÃO: Usamos o método getTooltip, que é a forma correta de gerar a lista de textos.
            List<Text> tooltipLines = this.focusedStack.getTooltip(client.player, client.options.advancedItemTooltips ? TooltipContext.Default.ADVANCED : TooltipContext.Default.BASIC);
            
            TooltipManager.cacheTooltip(this.focusedStack, tooltipLines);
        }
    }
}