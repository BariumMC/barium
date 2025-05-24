package com.barium.client.optimization;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack; // Import para ItemStack
// import net.minecraft.entity.player.PlayerInventory; // Geralmente não é necessário importar diretamente se você só acessa via PlayerEntity.getInventory()
import com.barium.client.mixin.accessor.PlayerInventoryAccessor; // APENAS SE NECESSÁRIO (ver nota acima)

/**
 * Utilitário para rastrear o estado do HUD para otimizações de dirty flag.
 * Mantém o último estado conhecido de elementos do HUD e marca flags quando eles mudam,
 * sinalizando a necessidade de redesenho.
 */
public class HudStateTracker {
    // --- Campos para armazenar o último estado conhecido dos elementos do HUD ---
    private static int lastHealth = -1;
    private static int lastFood = -1;
    private static int lastArmor = -1;
    private static int lastAir = -1;
    private static int lastExperience = -1;
    private static float lastExperienceProgress = -1.0f;
    private static int lastHeldItemStackHash = 0; // Hash do ItemStack na mão principal
    private static int lastHotbarSelection = -1; // Slot selecionado da hotbar

    // --- Flags de "dirty" (sujo/precisa redesenhar) ---
    // Definidas como true por padrão para garantir que o HUD seja renderizado na primeira vez.
    public static boolean healthDirty = true;
    public static boolean foodDirty = true;
    public static boolean armorDirty = true;
    public static boolean airDirty = true;
    public static boolean experienceDirty = true;
    public static boolean heldItemDirty = true;
    public static boolean hotbarSelectionDirty = true;

    /**
     * Atualiza o estado do jogador e marca as flags de dirty correspondentes.
     * Este método deve ser chamado uma vez por tick, idealmente no `tick()` do `InGameHud`.
     * @param player O PlayerEntity atual.
     */
    public static void updatePlayerState(PlayerEntity player) {
        // Verifica e atualiza a vida
        int currentHealth = (int) player.getHealth();
        if (currentHealth != lastHealth) {
            lastHealth = currentHealth;
            healthDirty = true;
        }

        // Verifica e atualiza a barra de fome
        int currentFood = player.getHungerManager().getFoodLevel();
        if (currentFood != lastFood) {
            lastFood = currentFood;
            foodDirty = true;
        }

        // Verifica e atualiza a barra de armadura
        int currentArmor = player.getArmor();
        if (currentArmor != lastArmor) {
            lastArmor = currentArmor;
            armorDirty = true;
        }

        // Verifica e atualiza a barra de ar (subaquática)
        int currentAir = player.getAir();
        if (currentAir != lastAir) {
            lastAir = currentAir;
            airDirty = true;
        }

        // Verifica e atualiza a barra de experiência e nível
        int currentExperience = player.experienceLevel;
        float currentExperienceProgress = player.experienceProgress;
        if (currentExperience != lastExperience || currentExperienceProgress != lastExperienceProgress) {
            lastExperience = currentExperience;
            lastExperienceProgress = currentExperienceProgress;
            experienceDirty = true;
        }

        // Verifica e atualiza o item na mão principal (mudança de hash)
        ItemStack mainHandStack = player.getMainHandStack();
        int currentHeldItemStackHash = mainHandStack.hashCode(); 
        if (currentHeldItemStackHash != lastHeldItemStackHash) {
            lastHeldItemStackHash = currentHeldItemStackHash;
            heldItemDirty = true;
        }
        
        // Verifica e atualiza a seleção da hotbar
        // Acesso direto a 'selectedSlot' da PlayerInventory.
        // Se o erro 'selectedSlot has private access' persistir aqui,
        // você precisará usar o Mixin Accessor 'PlayerInventoryAccessor' (conforme as notas anteriores).
        int currentHotbarSelection = player.getInventory().selectedSlot; 
        if (currentHotbarSelection != lastHotbarSelection) {
            lastHotbarSelection = currentHotbarSelection;
            hotbarSelectionDirty = true;
        }
    }

    /**
     * Marca todas as flags de "dirty" como true.
     * Útil quando o HUD precisa ser completamente redesenhado (ex: troca de mundo, alteração de configurações).
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