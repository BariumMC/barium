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
        // Usar "debug_left" para que shouldSkipRender possa aplicar lógica específica para o Debug HUD
        if (HudOptimizer.shouldSkipRender("debug_left")) {
            cir.setReturnValue(HudOptimizer.getCachedDebugHudText("debug_left"));
            return;
        }
        if (!HudOptimizer.shouldRecalculateDebugHud("debug_left")) {
            cir.setReturnValue(HudOptimizer.getCachedDebugHudText("debug_left"));
        }
    }

    @Inject(method = "getLeftText()Ljava/util/List;", at = @At("RETURN"))
    private void barium$getLeftTextReturn(CallbackInfoReturnable<List<String>> cir) {
        HudOptimizer.updateDebugHudCache("debug_left", cir.getReturnValue());
    }

    @Inject(method = "getRightText()Ljava/util/List;", at = @At("HEAD"), cancellable = true)
    private void barium$getRightTextHead(CallbackInfoReturnable<List<String>> cir) {
        // Usar "debug_right" para que shouldSkipRender possa aplicar lógica específica para o Debug HUD
        if (HudOptimizer.shouldSkipRender("debug_right")) {
            cir.setReturnValue(HudOptimizer.getCachedDebugHudText("debug_right"));
            return;
        }
        if (!HudOptimizer.shouldRecalculateDebugHud("debug_right")) {
            cir.setReturnValue(HudOptimizer.getCachedDebugHudText("debug_right"));
        }
    }

    @Inject(method = "getRightText()Ljava/util/List;", at = @At("RETURN"))
    private void barium$getRightTextReturn(CallbackInfoReturnable<List<String>> cir) {
        HudOptimizer.updateDebugHudCache("debug_right", cir.getReturnValue());
    }
}