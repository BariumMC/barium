package com.barium.client.mixin;

import com.barium.client.optimization.HudOptimizer;
import net.minecraft.client.gui.hud.DebugHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

/**
 * Mixin para DebugHud para otimizar a obtenção e renderização das informações de debug (F3).
 * Revisado para compatibilidade com mappings Yarn 1.21.5+build.1.
 */
@Mixin(DebugHud.class)
public abstract class DebugHudMixin {

    /**
     * Injeta no início do método getLeftText para potencialmente retornar texto do cache.
     * Se o cache for válido, o método original é cancelado e o texto do cache é retornado.
     *
     * Target Class: net.minecraft.client.gui.hud.DebugHud
     * Target Method Signature (Yarn 1.21.5+build.1): getLeftText()Ljava/util/List;
     */
    @Inject(
        method = "getLeftText()Ljava/util/List;",
        at = @At("HEAD"),
        cancellable = true
    )
    private void barium$getLeftTextHead(CallbackInfoReturnable<List<String>> cir) {
        if (!HudOptimizer.shouldRecalculateDebugHud("left")) {
            // Se o cache é válido e não precisa recalcular, retorna o texto do cache
            cir.setReturnValue(HudOptimizer.getCachedDebugHudText("left"));
            // BariumMod.LOGGER.debug("DebugHudMixin: Retornando leftText do cache."); // Para depuração
        }
    }

    /**
     * Injeta no final do método getLeftText para capturar o texto gerado pelo método original
     * e armazená-lo no cache para uso futuro.
     *
     * Target Class: net.minecraft.client.gui.hud.DebugHud
     * Target Method Signature (Yarn 1.21.5+build.1): getLeftText()Ljava/util/List;
     */
    @Inject(
        method = "getLeftText()Ljava/util/List;",
        at = @At("RETURN")
    )
    private void barium$getLeftTextReturn(CallbackInfoReturnable<List<String>> cir) {
        // O método original já foi executado e seu resultado está em cir.getReturnValue()
        HudOptimizer.updateDebugHudCache("left", cir.getReturnValue());
        // BariumMod.LOGGER.debug("DebugHudMixin: Atualizando cache de leftText."); // Para depuração
    }

    /**
     * Injeta no início do método getRightText para potencialmente retornar texto do cache.
     * Se o cache for válido, o método original é cancelado e o texto do cache é retornado.
     *
     * Target Class: net.minecraft.client.gui.hud.DebugHud
     * Target Method Signature (Yarn 1.21.5+build.1): getRightText()Ljava/util/List;
     */
    @Inject(
        method = "getRightText()Ljava/util/List;",
        at = @At("HEAD"),
        cancellable = true
    )
    private void barium$getRightTextHead(CallbackInfoReturnable<List<String>> cir) {
        if (!HudOptimizer.shouldRecalculateDebugHud("right")) {
            // Se o cache é válido e não precisa recalcular, retorna o texto do cache
            cir.setReturnValue(HudOptimizer.getCachedDebugHudText("right"));
            // BariumMod.LOGGER.debug("DebugHudMixin: Retornando rightText do cache."); // Para depuração
        }
    }

    /**
     * Injeta no final do método getRightText para capturar o texto gerado pelo método original
     * e armazená-lo no cache para uso futuro.
     *
     * Target Class: net.minecraft.client.gui.hud.DebugHud
     * Target Method Signature (Yarn 1.21.5+build.1): getRightText()Ljava/util/List;
     */
    @Inject(
        method = "getRightText()Ljava/util/List;",
        at = @At("RETURN")
    )
    private void barium$getRightTextReturn(CallbackInfoReturnable<List<String>> cir) {
        // O método original já foi executado e seu resultado está em cir.getReturnValue()
        HudOptimizer.updateDebugHudCache("right", cir.getReturnValue());
        // BariumMod.LOGGER.debug("DebugHudMixin: Atualizando cache de rightText."); // Para depuração
    }
}