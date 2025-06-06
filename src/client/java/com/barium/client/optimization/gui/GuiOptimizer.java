package com.barium.client.optimization.gui;

import com.barium.BariumMod;
import com.barium.config.BariumConfig;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;

import java.util.List;
import java.util.Objects;

/**
 * Otimiza a renderização de interfaces de usuário (GUI), como inventários.
 * A funcionalidade principal será desenvolvida aqui, como caching de elementos estáticos
 * e redução de chamadas de redesenho.
 */
public class GuiOptimizer {

    // Cache para a tooltip da GUI
    private static List<Text> cachedTooltip;
    private static Slot cachedSlot;
    private static ItemStack cachedItemStack;


    /**
     * Inicializa o GuiOptimizer.
     * Chamado na inicialização do cliente do mod.
     */
    public static void init() {
        if (!BariumConfig.ENABLE_GUI_OPTIMIZATION) {
            BariumMod.LOGGER.info("Otimização de GUI desativada.");
            return;
        }

        BariumMod.LOGGER.info("GuiOptimizer inicializado.");

        // Lógica de inicialização do cache da GUI pode ser adicionada aqui.
        // Por exemplo, registrar listeners para eventos de abertura/fechamento de tela
        // para gerenciar o ciclo de vida do cache.
    }

    /**
     * Verifica se a tooltip deve ser recalculada ou se a versão em cache pode ser usada.
     * @param screen A tela atual.
     * @param slot O slot atualmente sob o cursor.
     * @return true se o cache for válido e a tooltip não precisar ser recalculada.
     */
    public static boolean shouldUseCachedTooltip(HandledScreen<?> screen, Slot slot) {
        if (!BariumConfig.ENABLE_GUI_OPTIMIZATION || slot == null || !slot.hasStack()) {
            clearTooltipCache();
            return false;
        }

        // Verifica se o slot é o mesmo e o itemstack não mudou (comparando o item, contagem e NBT)
        if (slot == cachedSlot && ItemStack.areEqual(slot.getStack(), cachedItemStack)) {
            return true;
        }
        
        // Se chegou aqui, o cache está inválido
        clearTooltipCache();
        return false;
    }

    /**
     * Obtém a tooltip em cache.
     * @return A lista de Text da tooltip em cache.
     */
    public static List<Text> getCachedTooltip() {
        return cachedTooltip;
    }

    /**
     * Atualiza o cache com a nova tooltip e o estado do slot.
     * @param slot O slot que gerou a tooltip.
     * @param tooltip A lista de Text da tooltip.
     */
    public static void cacheTooltip(Slot slot, List<Text> tooltip) {
         if (!BariumConfig.ENABLE_GUI_OPTIMIZATION) return;

         cachedSlot = slot;
         cachedTooltip = tooltip;
         // É crucial copiar o itemstack, pois a instância pode ser mutável.
         cachedItemStack = slot.getStack().copy();
    }

    /**
     * Limpa o cache da tooltip.
     * Deve ser chamado ao fechar uma tela ou quando o cursor não está sobre um slot.
     */
    public static void clearTooltipCache() {
        cachedTooltip = null;
        cachedSlot = null;
        cachedItemStack = null;
    }
}