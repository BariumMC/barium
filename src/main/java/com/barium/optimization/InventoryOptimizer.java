package com.barium.optimization;

import com.barium.BariumMod;
import com.barium.config.BariumConfig;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * Otimiza verificações em inventários e containers, usando cache de slots vazios.
 * Baseado nos mappings Yarn 1.21.5+build.1
 */
public class InventoryOptimizer {

    // Cache para informações sobre slots vazios em inventários
    // Usamos WeakHashMap para permitir que os inventários sejam coletados pelo GC se não referenciados
    private static final Map<Inventory, InventoryState> INVENTORY_STATE_CACHE = new WeakHashMap<>();
    private static final long CACHE_DURATION_MS = 1000; // Cache válido por 1 segundo

    /**
     * Verifica se uma busca completa no inventário pode ser evitada.
     * Útil para operações como `canInsert` ou `transfer`.
     *
     * @param inventory O inventário.
     * @param stackToInsert O ItemStack que se deseja inserir (opcional, para verificações mais específicas).
     * @return true se a busca completa pode ser pulada (ex: cache indica que está cheio).
     */
    public static boolean canSkipFullInventoryCheck(Inventory inventory, ItemStack stackToInsert) {
        if (!BariumConfig.ENABLE_INVENTORY_OPTIMIZATION || !BariumConfig.CACHE_EMPTY_SLOTS) {
            return false;
        }

        InventoryState state = INVENTORY_STATE_CACHE.get(inventory);

        // Verifica o cache
        if (state != null && state.isValid()) {
            // Se o cache indica que não há slots vazios, podemos pular a busca
            if (state.firstEmptySlot == -1) {
                // Se estamos tentando inserir um item, precisamos verificar se há stacks compatíveis para merge
                // Se não estamos inserindo (apenas checando se está cheio), podemos pular.
                if (stackToInsert == null || stackToInsert.isEmpty()) {
                    // BariumMod.LOGGER.debug("Skipping full check: Cache indicates inventory is full.");
                    return true; // Cache diz que está cheio
                }
                // Se stackToInsert não é nulo, ainda precisamos verificar merge, então não pulamos só com base nisso.
            }
            // Poderíamos adicionar mais lógica aqui, como pular se o item a inserir não pode empilhar
            // e o cache indica que não há slots vazios.
        }

        return false; // Cache inválido ou não conclusivo, faça a verificação completa
    }

    /**
     * Atualiza o cache do estado do inventário após uma modificação.
     *
     * @param inventory O inventário modificado.
     */
    public static void updateInventoryCache(Inventory inventory) {
        if (!BariumConfig.ENABLE_INVENTORY_OPTIMIZATION || !BariumConfig.CACHE_EMPTY_SLOTS) {
            return;
        }

        int firstEmpty = -1;
        int size = inventory.size();
        for (int i = 0; i < size; ++i) {
            if (inventory.getStack(i).isEmpty()) {
                firstEmpty = i;
                break;
            }
        }

        INVENTORY_STATE_CACHE.put(inventory, new InventoryState(firstEmpty));
        // BariumMod.LOGGER.debug("Inventory cache updated. First empty slot: {}", firstEmpty);
    }

    /**
     * Obtém o índice do primeiro slot vazio do cache, se disponível.
     *
     * @param inventory O inventário.
     * @return O índice do primeiro slot vazio, ou -1 se cheio ou cache inválido.
     */
    public static int getFirstEmptySlotFromCache(Inventory inventory) {
        if (!BariumConfig.ENABLE_INVENTORY_OPTIMIZATION || !BariumConfig.CACHE_EMPTY_SLOTS) {
            return -1; // Retorna -1 para indicar que a busca normal deve ser feita
        }

        InventoryState state = INVENTORY_STATE_CACHE.get(inventory);
        if (state != null && state.isValid()) {
            return state.firstEmptySlot;
        }
        return -1; // Cache inválido
    }
    
    /**
     * Limpa o cache de um inventário específico.
     * 
     * @param inventory O inventário.
     */
    public static void invalidateInventoryCache(Inventory inventory) {
        INVENTORY_STATE_CACHE.remove(inventory);
    }

    /**
     * Limpa todo o cache (ex: ao fechar o mundo).
     */
    public static void clearAllCaches() {
        INVENTORY_STATE_CACHE.clear();
    }

    // --- Classe interna para o Estado do Inventário ---

    private static class InventoryState {
        final int firstEmptySlot; // -1 se não houver slots vazios
        final long timestamp;

        InventoryState(int firstEmptySlot) {
            this.firstEmptySlot = firstEmptySlot;
            this.timestamp = System.currentTimeMillis();
        }

        boolean isValid() {
            return (System.currentTimeMillis() - timestamp) < CACHE_DURATION_MS;
        }
    }
}
