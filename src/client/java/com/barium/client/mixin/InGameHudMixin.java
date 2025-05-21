package com.barium.client.mixin;

import com.barium.client.optimization.HudOptimizer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class InGameHudMixin {

    @Redirect(
        method = "render",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/hud/InGameHud;renderStatusEffectOverlay(Lnet/minecraft/client/gui/DrawContext;)V"
        )
    )
    private void redirectRenderStatusEffectOverlay(InGameHud instance, DrawContext context) {
        if (HudOptimizer.shouldUpdateHudElement("status_effects", () -> "effects")) {
            ((InGameHudAccessor) instance).barium$invokeRenderStatusEffectOverlay(context);
        }
    }
    
    @Inject(method = "renderStatusBars", at = @At("HEAD"), cancellable = true)
    private void onRenderStatusBars(MatrixStack matrices, CallbackInfo ci) {
        // Verifica se as barras de status devem ser atualizadas neste frame
        if (!HudOptimizer.shouldUpdateHudElement("status_bars", () -> "bars")) {
            ci.cancel(); // Usa o cache em vez de redesenhar
        }
    }
}
