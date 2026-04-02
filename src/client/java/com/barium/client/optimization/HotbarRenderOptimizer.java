package com.barium.client.optimization;

import com.barium.config.BariumConfig;
import net.minecraft.item.ItemStack;

import java.util.IdentityHashMap;

/**
 * Cache de curto prazo (por frame de HUD) para reduzir chamadas repetidas de
 * ItemStack.hasGlint em hotbar/HUD hooks que consultam o mesmo stack várias vezes.
 */
public final class HotbarRenderOptimizer {
    private static final IdentityHashMap<ItemStack, Boolean> GLINT_CACHE = new IdentityHashMap<>();
    private static int hudFrameIndex = 0;

    private HotbarRenderOptimizer() {
    }

    public static void beginHudFrame() {
        if (!BariumConfig.C.ENABLE_GUI_OPTIMIZATION) return;

        hudFrameIndex++;
        GLINT_CACHE.clear();
    }

    public static int currentHudFrame() {
        return hudFrameIndex;
    }

    public static Boolean getCachedGlint(ItemStack stack) {
        if (!BariumConfig.C.ENABLE_GUI_OPTIMIZATION || stack == null) return null;
        return GLINT_CACHE.get(stack);
    }

    public static void cacheGlint(ItemStack stack, boolean value) {
        if (!BariumConfig.C.ENABLE_GUI_OPTIMIZATION || stack == null) return;
        GLINT_CACHE.put(stack, value);
    }
}
