// src/client/java/com/barium/client/optimization/HudStateTracker.java
package com.barium.client.optimization;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack; // Import para ItemStack

// Import para PlayerInventoryAccessor (APENAS SE O ERRO 'selectedSlot' PERSISTIR)
// import com.barium.client.mixin.accessor.PlayerInventoryAccessor; 

/**
 * Utilitário para rastrear o estado do HUD para otimizações de dirty flag.
 */
public class HudStateTracker {
    // Estados das barras de HUD
    private static int lastHealth = -1;
    private static int lastFood = -1;
    private static int lastArmor = -1;
    private static int lastAir = -1;
    private static int lastExperience = -1;
    private static float lastExperienceProgress = -1.0f;
    private static int lastHeldItemStackHash = 0; // Para otimização de item segurado
    private static int lastHotbarSelection = -1; // Para seleção da hotbar

    // Flags de "sujeira" (dirty)
    public static boolean healthDirty = true;
    public static boolean foodDirty = true;
    public static boolean armorDirty = true;
    public static boolean airDirty = true;
    public static boolean experienceDirty = true;
    public static boolean heldItemDirty = true;
    public static boolean hotbarSelectionDirty = true; // Adicionado para seleção da hotbar

    /**
     * Atualiza o estado do jogador e marca as flags de dirty correspondentes.
     * Deve ser chamado uma vez por tick, por exemplo, no tick do InGameHud.
     * @param player O jogador atual.
     */
    public static void updatePlayerState(PlayerEntity player) {
        int currentHealth = (int) player.getHealth();
        if (currentHealth != lastHealth) {
            lastHealth = currentHealth;
            healthDirty = true;
        }

        int currentFood = player.getHungerManager().getFoodLevel();
        if (currentFood != lastFood) {
            lastFood = currentFood;
            foodDirty = true;
        }

        int currentArmor = player.getArmor();
        if (currentArmor != lastArmor) {
            lastArmor = currentArmor;
            armorDirty = true;
        }

        int currentAir = player.getAir();
        if (currentAir != lastAir) {
            lastAir = currentAir;
            airDirty = true;
        }

        int currentExperience = player.experienceLevel;
        float currentExperienceProgress = player.experienceProgress;
        if (currentExperience != lastExperience || currentExperienceProgress != lastExperienceProgress) {
            lastExperience = currentExperience;
            lastExperienceProgress = currentExperienceProgress;
            experienceDirty = true;
        }

        ItemStack mainHandStack = player.getMainHandStack();
        int currentHeldItemStackHash = mainHandStack.hashCode(); // Usa hashCode do ItemStack
        if (currentHeldItemStackHash != lastHeldItemStackHash) {
            lastHeldItemStackHash = currentHeldItemStackHash;
            heldItemDirty = true;
        }
        
        // Acesso a selectedSlot:
        // Na maioria dos casos, player.getInventory().selectedSlot é público o suficiente para Loom.
        // SE O ERRO 'selectedSlot has private access' PERSISTIR AQUI:
        // Descomente a linha 'import com.barium.client.mixin.accessor.PlayerInventoryAccessor;' acima
        // e mude a linha abaixo para usar o accessor:
        // int currentHotbarSelection = ((PlayerInventoryAccessor)player.getInventory()).getSelectedSlot();
        int currentHotbarSelection = player.getInventory().selectedSlot; 
        if (currentHotbarSelection != lastHotbarSelection) {
            lastHotbarSelection = currentHotbarSelection;
            hotbarSelectionDirty = true;
        }
    }

    /**
     * Marca todas as flags de dirty como true.
     * Útil quando o HUD precisa ser completamente redesenhado (ex: troca de mundo).
     */
    public static void markAllHudDirty() {
        healthDirty = true;
        foodDirty = true;
        armorDirty = true;
        airDirty = true;
        experienceDirty = true;
        heldItemDirty = true;
        hotbarSelectionDirty = true;
    }
}