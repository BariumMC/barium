package com.barium.client.optimization;

import com.barium.config.BariumConfig;
import net.minecraft.item.ItemStack;

import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Cache de curto prazo (por frame de HUD) para reduzir chamadas repetidas de
 * ItemStack.hasGlint em hotbar/HUD hooks que consultam o mesmo stack várias vezes.
 */
public final class HotbarRenderOptimizer {
    private static final IdentityHashMap<ItemStack, GlintCacheEntry> GLINT_CACHE = new IdentityHashMap<>();
    private static int hudFrameIndex = 0;
    private static final int GLINT_CACHE_TTL_FRAMES = 10;

    private HotbarRenderOptimizer() {
    }

    public static void beginHudFrame() {
        if (!BariumConfig.C.ENABLE_GUI_OPTIMIZATION) return;

        hudFrameIndex++;
        if ((hudFrameIndex & 7) == 0) {
            pruneExpired();
        }
    }

    public static int currentHudFrame() {
        return hudFrameIndex;
    }

    public static Boolean getCachedGlint(ItemStack stack) {
        if (!BariumConfig.C.ENABLE_GUI_OPTIMIZATION || stack == null) return null;
        GlintCacheEntry entry = GLINT_CACHE.get(stack);
        if (entry == null) return null;

        if ((hudFrameIndex - entry.frame) > GLINT_CACHE_TTL_FRAMES) {
            GLINT_CACHE.remove(stack);
            return null;
        }
        if (entry.count != stack.getCount() || entry.damage != stack.getDamage()) {
            return null;
        }
        if (entry.hasEnchantments != stack.hasEnchantments()) {
            return null;
        }

        return entry.glint;
    }

    public static void cacheGlint(ItemStack stack, boolean value) {
        if (!BariumConfig.C.ENABLE_GUI_OPTIMIZATION || stack == null) return;
        GLINT_CACHE.put(stack, new GlintCacheEntry(
                value,
                hudFrameIndex,
                stack.getCount(),
                stack.getDamage(),
                stack.hasEnchantments()
        ));
    }

    private static void pruneExpired() {
        Iterator<Map.Entry<ItemStack, GlintCacheEntry>> it = GLINT_CACHE.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<ItemStack, GlintCacheEntry> entry = it.next();
            if ((hudFrameIndex - entry.getValue().frame) > GLINT_CACHE_TTL_FRAMES) {
                it.remove();
            }
        }
    }

    private record GlintCacheEntry(boolean glint, int frame, int count, int damage, boolean hasEnchantments) {
    }
}
