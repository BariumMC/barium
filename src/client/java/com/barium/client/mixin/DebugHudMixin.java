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
 * Corrigido: Habilita o cache de texto do DebugHud.
 */
@Mixin(DebugHud.class)
public abstract class DebugHudMixin {

    /**
     * Injeta no início do método getLeftText para potencialmente retornar texto do cache.
     *
     * Target Class: net.minecraft.client.gui.hud.DebugHud
     * Target Method Signature (Yarn 1.21.5+build.1): getLeftText()Ljava/util/List;
     */
    @Inject(
        method = "getLeftText()Ljava/util/List;", // Confirmado com Yarn 1.21.5+build.1
        at = @At("HEAD"),
        cancellable = true
    )
    private void barium$getLeftText(CallbackInfoReturnable<List<String>> cir) {
        List<String> cachedText = HudOptimizer.getCachedDebugHudText((DebugHud)(Object)this, "left");

        if (cachedText != null && !cachedText.isEmpty()) {
            cir.setReturnValue(cachedText); // Use o cache se for válido
        }
    }

    /**
     * Injeta no final do método getLeftText para atualizar o cache.
     *
     * Target Class: net.minecraft.client.gui.hud.DebugHud
     * Target Method Signature (Yarn 1.21.5+build.1): getLeftText()Ljava/util/List;
     */
    @Inject(
        method = "getLeftText()Ljava/util/List;",
        at = @At("RETURN")
    )
    private void barium$cacheLeftText(CallbackInfoReturnable<List<String>> cir) {
        // Captura o valor de retorno e atualiza o cache
        HudOptimizer.updateDebugHudCache("left", cir.getReturnValue());
    }


    /**
     * Injeta no início do método getRightText para potencialmente retornar texto do cache.
     *
     * Target Class: net.minecraft.client.gui.hud.DebugHud
     * Target Method Signature (Yarn 1.21.5+build.1): getRightText()Ljava/util/List;
     */
    @Inject(
        method = "getRightText()Ljava/util/List;", // Confirmado com Yarn 1.21.5+build.1
        at = @At("HEAD"),
        cancellable = true
    )
    private void barium$getRightText(CallbackInfoReturnable<List<String>> cir) {
        List<String> cachedText = HudOptimizer.getCachedDebugHudText((DebugHud)(Object)this, "right");

        if (cachedText != null && !cachedText.isEmpty()) {
            cir.setReturnValue(cachedText); // Use o cache se for válido
        }
    }

    /**
     * Injeta no final do método getRightText para atualizar o cache.
     *
     * Target Class: net.minecraft.client.gui.hud.DebugHud
     * Target Method Signature (Yarn 1.21.5+build.1): getRightText()Ljava/util/List;
     */
    @Inject(
        method = "getRightText()Ljava/util/List;",
        at = @At("RETURN")
    )
    private void barium$cacheRightText(CallbackInfoReturnable<List<String>> cir) {
        // Captura o valor de retorno e atualiza o cache
        HudOptimizer.updateDebugHudCache("right", cir.getReturnValue());
    }
}