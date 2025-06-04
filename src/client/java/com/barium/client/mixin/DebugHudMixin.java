package com.barium.client.mixin;

import com.barium.client.optimization.HudOptimizer;
import net.minecraft.client.gui.hud.DebugHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(DebugHud.class)
public abstract class DebugHudMixin {

    @Inject(method = "getLeftText()Ljava/util/List;", at = @At("HEAD"), cancellable = true)
    private void barium$getLeftTextHead(CallbackInfoReturnable<List<String>> cir) {
        if (HudOptimizer.shouldSkipRender("left")) {
            cir.setReturnValue(HudOptimizer.getCachedDebugHudText("left"));
            return;
        }
        if (!HudOptimizer.shouldRecalculateDebugHud("left")) {
            cir.setReturnValue(HudOptimizer.getCachedDebugHudText("left"));
        }
    }

    @Inject(method = "getLeftText()Ljava/util/List;", at = @At("RETURN"))
    private void barium$getLeftTextReturn(CallbackInfoReturnable<List<String>> cir) {
        HudOptimizer.updateDebugHudCache("left", cir.getReturnValue());
    }

    @Inject(method = "getRightText()Ljava/util/List;", at = @At("HEAD"), cancellable = true)
    private void barium$getRightTextHead(CallbackInfoReturnable<List<String>> cir) {
        if (HudOptimizer.shouldSkipRender("right")) {
            cir.setReturnValue(HudOptimizer.getCachedDebugHudText("right"));
            return;
        }
        if (!HudOptimizer.shouldRecalculateDebugHud("right")) {
            cir.setReturnValue(HudOptimizer.getCachedDebugHudText("right"));
        }
    }

    @Inject(method = "getRightText()Ljava/util/List;", at = @At("RETURN"))
    private void barium$getRightTextReturn(CallbackInfoReturnable<List<String>> cir) {
        HudOptimizer.updateDebugHudCache("right", cir.getReturnValue());
    }
}
