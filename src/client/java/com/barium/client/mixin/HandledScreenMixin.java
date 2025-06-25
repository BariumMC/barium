package com.barium.client.mixin;

import com.barium.client.util.TooltipManager;
import com.barium.config.BariumConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin {

    @Shadow protected ItemStack an; // Nome ofuscado para 'focusedStack'

    /**
     * Injeta ANTES de a tooltip ser renderizada.
     * Aqui, verificamos nosso cache. Se tivermos uma tooltip válida, nós a desenhamos
     * e cancelamos o método original para evitar que ela seja gerada novamente.
     */
    @Inject(
        method = "render(Lnet/minecraft/client/gui/DrawContext;IIF)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ingame/HandledScreen;renderTooltip(Lnet/minecraft/client/gui/DrawContext;II)V"),
        cancellable = true
    )
    private void barium$useCachedTooltip(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (!BariumConfig.C.ENABLE_TOOLTIP_CACHING) return;
        
        if (TooltipManager.hasCachedTooltip(this.an)) {
            // Desenha a tooltip em cache
            context.drawTooltip(MinecraftClient.getInstance().textRenderer, TooltipManager.getCachedTooltip(), mouseX, mouseY);
            // Cancela a chamada ao `renderTooltip` original
            ci.cancel();
        } else {
            // Se não temos cache, limpa o cache antigo para garantir.
            TooltipManager.clearCache();
        }
    }

    /**
     * Injeta DEPOIS que a tooltip é gerada e desenhada.
     * Aqui, nós armazenamos em cache a tooltip que acabou de ser criada.
     */
    @Inject(
        method = "drawMouseoverTooltip(Lnet/minecraft/client/gui/DrawContext;II)V",
        at = @At("TAIL")
    )
    private void barium$cacheNewTooltip(DrawContext context, int x, int y, CallbackInfo ci) {
        if (!BariumConfig.C.ENABLE_TOOLTIP_CACHING) return;

        // Após a tooltip ser desenhada, this.an (focusedStack) está definido.
        if (!this.an.isEmpty()) {
            // O método getTooltip() gera a lista de textos.
            TooltipManager.cacheTooltip(this.an, this.an.getTooltipData().getTooltipText());
        }
    }
}