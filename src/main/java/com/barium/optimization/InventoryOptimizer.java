package com.barium.optimization;

import com.barium.BariumMod;
import com.barium.config.BariumConfig;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Otimizador de inventários e containers.
 * 
 * Implementa:
 * - Cache de slots vazios
 * - Otimização de verificação de inventários
 */
public class InventoryOptimizer {
    // Cache de slots vazios por inventário
    private static final Map<Inventory, Set<Integer>> EMPTY_SLOTS_CACHE = new HashMap<>();
    
    // Hash do último estado conhecido do inventário
    private static final Map<Inventory, Integer> INVENTORY_HASH = new HashMap<>();
    
    public static void init() {
        BariumMod.LOGGER.info("Inicializando otimizações de inventários e containers");
    }
    
    /**
     * Atualiza o cache de slots vazios para um inventário
     * 
     * @param inventory O inventário
     */
    public static void updateEmptySlotsCache(Inventory inventory) {
        if (!BariumConfig.ENABLE_SLOT_CACHING) {
            return;
        }
        
        // Calcula o hash atual do inventário
        int currentHash = calculateInventoryHash(inventory);
        
        // Verifica se o inventário mudou desde a última verificação
        Integer lastHash = INVENTORY_HASH.get(inventory);
        if (lastHash != null && lastHash == currentHash) {
            // Inventário não mudou, não precisa atualizar o cache
            return;
        }
        
        // Atualiza o hash
        INVENTORY_HASH.put(inventory, currentHash);
        
        // Cria ou limpa o conjunto de slots vazios
        Set<Integer> emptySlots = EMPTY_SLOTS_CACHE.computeIfAbsent(inventory, k -> new HashSet<>());
        emptySlots.clear();
        
        // Identifica todos os slots vazios
        for (int i = 0; i < inventory.size(); i++) {
            if (inventory.getStack(i).isEmpty()) {
                emptySlots.add(i);
            }
        }
    }
    
    /**
     * Verifica se um slot está vazio, usando o cache quando possível
     * 
     * @param inventory O inventário
     * @param slot O índice do slot
     * @return true se o slot está vazio, false caso contrário
     */
    public static boolean isSlotEmpty(Inventory inventory, int slot) {
        if (!BariumConfig.ENABLE_SLOT_CACHING) {
            return inventory.getStack(slot).isEmpty();
        }
        
        // Obtém o cache de slots vazios
        Set<Integer> emptySlots = EMPTY_SLOTS_CACHE.get(inventory);
        if (emptySlots == null) {
            // Cache não inicializado, atualiza e verifica novamente
            updateEmptySlotsCache(inventory);
            emptySlots = EMPTY_SLOTS_CACHE.get(inventory);
        }
        
        return emptySlots.contains(slot);
    }
    
    /**
     * Encontra o primeiro slot vazio em um inventário
     * 
     * @param inventory O inventário
     * @return O índice do primeiro slot vazio, ou -1 se não houver slots vazios
     */
    public static int findFirstEmptySlot(Inventory inventory) {
        if (!BariumConfig.ENABLE_SLOT_CACHING) {
            // Implementação padrão sem cache
            for (int i = 0; i < inventory.size(); i++) {
                if (inventory.getStack(i).isEmpty()) {
                    return i;
                }
            }
            return -1;
        }
        
        // Obtém o cache de slots vazios
        Set<Integer> emptySlots = EMPTY_SLOTS_CACHE.get(inventory);
        if (emptySlots == null || emptySlots.isEmpty()) {
            // Cache não inicializado ou vazio, atualiza e verifica novamente
            updateEmptySlotsCache(inventory);
            emptySlots = EMPTY_SLOTS_CACHE.get(inventory);
            
            if (emptySlots == null || emptySlots.isEmpty()) {
                return -1;
            }
        }
        
        // Retorna o primeiro slot vazio (menor índice)
        return emptySlots.stream().min(Integer::compare).orElse(-1);
    }
    
    /**
     * Notifica que um slot foi modificado
     * 
     * @param inventory O inventário
     * @param slot O índice do slot
     * @param stack O novo ItemStack
     */
    public static void notifySlotChange(Inventory inventory, int slot, ItemStack stack) {
        if (!BariumConfig.ENABLE_SLOT_CACHING) {
            return;
        }
        
        // Obtém o cache de slots vazios
        Set<Integer> emptySlots = EMPTY_SLOTS_CACHE.get(inventory);
        if (emptySlots == null) {
            return;
        }
        
        // Atualiza o cache para este slot
        if (stack.isEmpty()) {
            emptySlots.add(slot);
        } else {
            emptySlots.remove(slot);
        }
        
        // Invalida o hash para forçar uma atualização completa na próxima verificação
        INVENTORY_HASH.remove(inventory);
    }
    
    /**
     * Calcula um hash simples do estado atual do inventário
     * 
     * @param inventory O inventário
     * @return Um valor de hash representando o estado atual
     */
    private static int calculateInventoryHash(Inventory inventory) {
        int hash = 0;
        
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.getStack(i);
            if (!stack.isEmpty()) {
                hash = 31 * hash + (i + 1);
                hash = 31 * hash + stack.getItem().hashCode();
                hash = 31 * hash + stack.getCount();
            }
        }
        
        return hash;
    }
    
    /**
     * Remove um inventário do sistema de cache
     * 
     * @param inventory O inventário a remover
     */
    public static void removeInventory(Inventory inventory) {
        EMPTY_SLOTS_CACHE.remove(inventory);
        INVENTORY_HASH.remove(inventory);
    }
}
