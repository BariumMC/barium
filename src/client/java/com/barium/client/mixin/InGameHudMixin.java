package com.barium.client.mixin;

import com.barium.client.optimization.HudOptimizer;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class InGameHudMixin {

     static {
        System.out.println("MixinInGameHud aplicado com sucesso!");
    }
    
@Redirect(method = "render", 
    at = @At(value = "INVOKE", 
             target = "Lnet/minecraft/client/gui/hud/InGameHud;renderStatusEffectOverlay(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/util/RenderTickCounter;)V"))
private void redirectRenderStatusEffectOverlay(InGameHud instance, DrawContext context, RenderTickCounter tickCounter) {
    if (HudOptimizer.shouldUpdateHudElement("status_effects", () -> "effects")) {
        instance.renderStatusEffectOverlay(context, tickCounter);
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
