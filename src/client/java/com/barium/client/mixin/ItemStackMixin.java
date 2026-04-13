package com.barium.client.mixin;

import com.barium.client.optimization.HotbarRenderOptimizer;
import com.barium.config.BariumConfig;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public class ItemStackMixin {
    @Unique
    private int barium$glintCacheFrame = Integer.MIN_VALUE;

    @Unique
    private boolean barium$glintCacheValue;

    @Inject(method = "hasGlint()Z", at = @At("HEAD"), cancellable = true)
    private void barium$usePerFrameGlintCache(CallbackInfoReturnable<Boolean> cir) {
        if (!BariumConfig.C.ENABLE_GUI_OPTIMIZATION) return;

        int frame = HotbarRenderOptimizer.currentHudFrame();
        if (frame == barium$glintCacheFrame) {
            cir.setReturnValue(barium$glintCacheValue);
            return;
        }

        ItemStack self = (ItemStack) (Object) this;
        Boolean cached = HotbarRenderOptimizer.getCachedGlint(self);
        if (cached != null) {
            barium$glintCacheFrame = frame;
            barium$glintCacheValue = cached;
            cir.setReturnValue(cached);
        }
    }

    @Inject(method = "hasGlint()Z", at = @At("RETURN"))
    private void barium$storePerFrameGlintCache(CallbackInfoReturnable<Boolean> cir) {
        if (!BariumConfig.C.ENABLE_GUI_OPTIMIZATION) return;

        ItemStack self = (ItemStack) (Object) this;
        boolean result = cir.getReturnValue();
        HotbarRenderOptimizer.cacheGlint(self, result);
        barium$glintCacheFrame = HotbarRenderOptimizer.currentHudFrame();
        barium$glintCacheValue = result;
    }
}
