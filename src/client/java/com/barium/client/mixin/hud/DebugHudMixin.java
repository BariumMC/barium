package com.barium.client.mixin.hud;

import com.barium.client.optimization.HudOptimizer;
import net.minecraft.client.gui.hud.DebugHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(DebugHud.class)
public abstract class DebugHudMixin {

    /**
     * Injeta no RETORNO do método getLeftText() para otimizar a geração das linhas da esquerda do Debug HUD.
     *
     * @param cir O CallbackInfo para controlar o retorno. O resultado do método original
     *            está em cir.getReturnValue().
     */
    @Inject(method = "getLeftText", at = @At("RETURN"), cancellable = true)
    private void barium$optimizeGetLeftText(CallbackInfoReturnable<List<String>> cir) {
        List<String> originalLines = cir.getReturnValue();
        List<String> optimizedLines = HudOptimizer.getOptimizedDebugInfo("debug_left", originalLines);
        
        // Se as linhas otimizadas são diferentes das originais (ou seja, foram cacheadas)
        if (optimizedLines != originalLines) {
            cir.setReturnValue(optimizedLines);
        }
    }

    /**
     * Injeta no RETORNO do método getRightText() para otimizar a geração das linhas da direita do Debug HUD.
     *
     * @param cir O CallbackInfo para controlar o retorno. O resultado do método original
     *            está em cir.getReturnValue().
     */
    @Inject(method = "getRightText", at = @At("RETURN"), cancellable = true)
    private void barium$optimizeGetRightText(CallbackInfoReturnable<List<String>> cir) {
        List<String> originalLines = cir.getReturnValue();
        List<String> optimizedLines = HudOptimizer.getOptimizedDebugInfo("debug_right", originalLines);
        
        // Se as linhas otimizadas são diferentes das originais (ou seja, foram cacheadas)
        if (optimizedLines != originalLines) {
            cir.setReturnValue(optimizedLines);
        }
    }
}