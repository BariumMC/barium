package com.barium.client.util;

import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import java.util.List;

public class TooltipManager {
    private static ItemStack cachedItemStack = ItemStack.EMPTY;
    private static List<Text> cachedTooltip;

    /**
     * Verifica se a tooltip para o ItemStack atual já está em cache.
     * @param stack O ItemStack sobre o qual o mouse está.
     * @return true se a tooltip em cache for válida para este stack.
     */
    public static boolean hasCachedTooltip(ItemStack stack) {
        // O cache é válido se o item for o mesmo e o cache não estiver vazio.
        return !stack.isEmpty() && ItemStack.areEqual(cachedItemStack, stack) && cachedTooltip != null;
    }

    public static List<Text> getCachedTooltip() {
        return cachedTooltip;
    }

    /**
     * Atualiza o cache com a nova tooltip gerada.
     * @param stack O ItemStack para o qual a tooltip foi gerada.
     * @param tooltip A lista de textos da tooltip.
     */
    public static void cacheTooltip(ItemStack stack, List<Text> tooltip) {
        cachedItemStack = stack.copy(); // Copia para evitar problemas com mutabilidade
        cachedTooltip = tooltip;
    }

    /**
     * Limpa o cache. Deve ser chamado quando o mouse não está mais sobre um item.
     */
    public static void clearCache() {
        cachedItemStack = ItemStack.EMPTY;
        cachedTooltip = null;
    }
}