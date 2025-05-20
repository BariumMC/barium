package com.barium.client.mixin;

import com.barium.client.optimization.HudOptimizer;
import net.minecraft.client.gui.hud.DebugHud;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DebugHud.class)
public class DebugHudMixin {
    
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void onRender(MatrixStack matrices, CallbackInfo ci) {
        // Verifica se o debug HUD deve ser atualizado neste frame
        if (!HudOptimizer.shouldUpdateHudElement("debug_full", () -> "debug_hud")) {
            ci.cancel(); // Usa o cache em vez de redesenhar
        }
    }
}
