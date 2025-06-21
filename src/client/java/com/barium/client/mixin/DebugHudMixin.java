// --- Substitua o conteúdo em: src/client/java/com/barium/client/mixin/DebugHudMixin.java ---
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

    @Inject(method = "getLeftText", at = @At("HEAD"), cancellable = true)
    private void barium$getLeftTextHead(CallbackInfoReturnable<List<String>> cir) {
        // CORREÇÃO: A única verificação necessária é se devemos recalcular ou usar o cache.
        if (!HudOptimizer.shouldRecalculateDebugHud("debug_left")) {
            cir.setReturnValue(HudOptimizer.getCachedDebugHudText("debug_left"));
        }
    }

    @Inject(method = "getLeftText", at = @At("RETURN"))
    private void barium$getLeftTextReturn(CallbackInfoReturnable<List<String>> cir) {
        HudOptimizer.updateDebugHudCache("debug_left", cir.getReturnValue());
    }

    @Inject(method = "getRightText", at = @At("HEAD"), cancellable = true)
    private void barium$getRightTextHead(CallbackInfoReturnable<List<String>> cir) {
        // CORREÇÃO: Mesma lógica para o lado direito.
        if (!HudOptimizer.shouldRecalculateDebugHud("debug_right")) {
            cir.setReturnValue(HudOptimizer.getCachedDebugHudText("debug_right"));
        }
    }

    @Inject(method = "getRightText", at = @At("RETURN"))
    private void barium$getRightTextReturn(CallbackInfoReturnable<List<String>> cir) {
        HudOptimizer.updateDebugHudCache("debug_right", cir.getReturnValue());
    }
}