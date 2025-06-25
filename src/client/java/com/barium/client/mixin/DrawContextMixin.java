package com.barium.client.mixin;

import com.barium.client.util.TooltipManager;
import com.barium.config.BariumConfig;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.tooltip.TooltipData;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Optional;

@Mixin(DrawContext.class)
public abstract class DrawContextMixin {

    // Precisamos de uma sombra para o `getStack` para saber a qual item a tooltip pertence.
    @Shadow public abstract ItemStack getStack();

    /**
     * Injeta no início do método que desenha a tooltip. Neste ponto, todos os argumentos
     * (incluindo a lista de textos) foram totalmente preparados pelo jogo.
     */
    @Inject(
        method = "drawTooltip(Lnet/minecraft/client/font/TextRenderer;Ljava/util/List;Ljava/util/Optional;II)V",
        at = @At("HEAD")
    )
    private void barium$cacheGeneratedTooltip(TextRenderer textRenderer, List<Text> lines, Optional<TooltipData> data, int x, int y, CallbackInfo ci) {
        if (!BariumConfig.C.ENABLE_TOOLTIP_CACHING) return;
        
        // Obtém a pilha de itens associada a esta chamada de desenho.
        ItemStack stack = this.getStack();
        
        // Se houver um item e a lista de textos não estiver vazia, armazena no cache.
        if (stack != null && !stack.isEmpty() && lines != null && !lines.isEmpty()) {
             // Apenas faz o cache se ele ainda não existir para este item,
             // evitando recriar o cache desnecessariamente.
            if (!TooltipManager.hasCachedTooltip(stack)) {
                TooltipManager.cacheTooltip(stack, lines);
            }
        }
    }
}