// barium-main/src/client/java/com/barium/client/mixin/InGameHudMixin.java
package com.barium.client.mixin;

import com.barium.client.optimization.HudOptimizer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin para InGameHud para otimizar a renderização de elementos como efeitos de status e barras.
 * Revisado para compatibilidade com mappings Yarn 1.21.5+build.1.
 */
@Mixin(InGameHud.class)
public abstract class InGameHudMixin {

    /**
     * Injeta no início do método renderStatusEffectOverlay para potencialmente pular a renderização se o cache estiver válido.
     * Target Class: net.minecraft.client.gui.hud.InGameHud
     * Target Method Signature (Yarn 1.21.5+build.1): renderStatusEffectOverlay(Lnet/minecraft/client/render/RenderTickCounter;Lnet/minecraft/client/gui/DrawContext;)V
     */
    @Inject(
        // Esta assinatura é a mais provável para 1.21.5, conforme verificado anteriormente.
        method = "renderStatusEffectOverlay",
        at = @At("HEAD"),
        cancellable = true
    )
    private void barium$onRenderStatusEffectsOverlay(RenderTickCounter tickCounter, DrawContext context, CallbackInfo ci) {
        if (!HudOptimizer.shouldUpdateHudElement("status_effects", () -> "effects_placeholder_state")) {
            ci.cancel();
        }
    }

    /**
     * Injeta no início do método renderStatusBars para potencialmente pular a renderização se o cache estiver válido.
     * Target Class: net.minecraft.client.gui.hud.InGameHud
     * Target Method Signature (Yarn 1.21.5+build.1 - CORRIGIDA): renderStatusBars(Lnet/minecraft/client/render/RenderTickCounter;Lnet/minecraft/client/gui/DrawContext;)V
     */
    @Inject(
        // CORREÇÃO AQUI: Removido o 'F' (float) da assinatura do método target
        method = "renderStatusBars",
        at = @At("HEAD"),
        cancellable = true
    )
    private void barium$onRenderStatusBars(RenderTickCounter tickCounter, DrawContext context, CallbackInfo ci) {
        if (!HudOptimizer.shouldUpdateHudElement("status_bars", () -> "bars_placeholder_state")) {
            ci.cancel();
        }
    }

    // TODO: Adicionar mais injeções para outros elementos da HUD (hotbar, crosshair, etc.) se necessário.
    // TODO: Implementar a lógica de cache e renderização no HudOptimizer.
}