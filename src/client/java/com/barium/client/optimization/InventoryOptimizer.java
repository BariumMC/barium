package com.barium.client.optimization;

import com.barium.BariumMod;
import com.barium.config.BariumConfig;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Otimiza a renderização de itens em inventários, redesenhando apenas slots cujos itens mudaram.
 * Baseado nos mappings Yarn 1.21.5+build.1.
 */
public class InventoryOptimizer {

    // Cache para o estado dos itens em cada slot
    // A chave é o Slot, o valor é o ItemStack que estava no slot na última renderização.
    // Usamos ConcurrentHashMap para segurança de threads, embora a maioria das interações
    // de renderização aconteça na thread principal.
    private static final Map<Slot, ItemStack> cachedSlotStates = new ConcurrentHashMap<>();

    /**
     * Inicializa o otimizador de inventário.
     */
    public static void init() {
        BariumMod.LOGGER.info("Inicializando InventoryOptimizer");
        clearCaches();
    }

    /**
     * Verifica se um slot individual precisa ser redesenhado.
     * Compara o ItemStack atual do slot com o ItemStack em cache.
     * Se forem diferentes (ou o cache não existir), o slot precisa ser redesenhado
     * e o cache é atualizado.
     *
     * @param slot O slot a ser verificado.
     * @return true se o slot precisa ser redesenhado, false caso contrário.
     */
    public static boolean shouldRedrawSlot(Slot slot) {
        if (!BariumConfig.ENABLE_INVENTORY_OPTIMIZATION || !BariumConfig.CACHE_INVENTORY_ITEMS) {
            return true; // Sempre redesenha se a otimização ou o cache estiverem desativados
        }

        ItemStack currentStack = slot.getStack();
        ItemStack cachedStack = cachedSlotStates.get(slot);

        // Verifica se o item atual é diferente do item em cache
        // ItemStack.areEqual compara tipo, NBT e contagem (essencialmente, se são o "mesmo" item).
        if (!ItemStack.areEqual(currentStack, cachedStack)) {
            // O item mudou, então precisa redesenhar.
            // Atualiza o cache com uma cópia do ItemStack atual para evitar problemas de referência.
            // É crucial usar .copy() porque ItemStack é mutável.
            cachedSlotStates.put(slot, currentStack.copy());
            // BariumMod.LOGGER.debug("InventoryOptimizer: Slot changed, redrawing. Slot: {}, Item: {}", slot.getIndex(), currentStack.getName().getString());
            return true;
        }
        // BariumMod.LOGGER.debug("InventoryOptimizer: Slot unchanged, skipping redraw. Slot: {}", slot.getIndex());
        return false; // O item não mudou, não precisa redesenhar este slot.
    }

    /**
     * Remove todos os caches de slots associados a um ScreenHandler específico.
     * Deve ser chamado quando uma tela de inventário é fechada para evitar vazamento de memória.
     *
     * @param screenHandler O ScreenHandler que foi fechado.
     */
    public static void onScreenClosed(ScreenHandler screenHandler) {
        if (!BariumConfig.ENABLE_INVENTORY_OPTIMIZATION || !BariumConfig.CACHE_INVENTORY_ITEMS) {
            return;
        }

        // Remove apenas os slots que pertencem a este screenHandler.
        // Isso é mais seguro do que limpar tudo, caso mais de uma tela possa estar aberta ou
        // se o cache contiver slots de outras fontes.
        screenHandler.slots.forEach(cachedSlotStates::remove);
        BariumMod.LOGGER.debug("InventoryOptimizer: Cache limpo para ScreenHandler: {}", screenHandler.getClass().getSimpleName());
    }

    /**
     * Limpa todos os caches de slots. Deve ser chamado em caso de reloads de recursos
     * ou para garantir um estado limpo.
     */
    public static void clearCaches() {
        cachedSlotStates.clear();
        BariumMod.LOGGER.info("InventoryOptimizer: Todos os caches de inventário foram limpos.");
    }
}