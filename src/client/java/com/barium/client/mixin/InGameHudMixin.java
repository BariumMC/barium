package com.barium.client.mixin;

import com.barium.client.optimization.HudOptimizer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {

    // Injeta no início do método renderStatusEffectOverlay
    // Nova Assinatura Tentativa para 1.21.5: renderStatusEffectOverlay(Lnet/minecraft/client/render/RenderTickCounter;Lnet/minecraft/client/gui/DrawContext;)V
    // Se o erro persistir, pode ser: renderStatusEffectOverlay(Lnet/minecraft/client/render/RenderTickCounter;FLnet/minecraft/client/gui/DrawContext;)V
    @Inject(
        method = "renderStatusEffectOverlay(Lnet/minecraft/client/render/RenderTickCounter;Lnet/minecraft/client/gui/DrawContext;)V", // Mantenha esta por enquanto se a abaixo der erro
        // TENTE ESTA SE A ACIMA FALHAR:
        // method = "renderStatusEffectOverlay(Lnet/minecraft/client/render/RenderTickCounter;FLnet/minecraft/client/gui/DrawContext;)V",
        at = @At("HEAD"),
        cancellable = true
    )
    //private void barium$onRenderStatusEffectsOverlay(RenderTickCounter tickCounter, DrawContext context, CallbackInfo ci) { // Removido float tickDelta para corresponder à assinatura mais provável
    // TENTE ESTA SE A ACIMA FALHAR:
    private void barium$onRenderStatusEffectsOverlay(RenderTickCounter tickCounter, float tickDelta, DrawContext context, CallbackInfo ci) { // Adicionado float tickDelta
        if (!HudOptimizer.shouldUpdateHudElement("status_effects", () -> "effects_placeholder_state")) {
            ci.cancel();
        }
    }

    // Injeta no início do método renderStatusBars
    // Nova Assinatura Tentativa para 1.21.5: renderStatusBars(Lnet/minecraft/client/render/RenderTickCounter;Lnet/minecraft/client/gui/DrawContext;)V
    // Se o erro persistir, pode ser: renderStatusBars(Lnet/minecraft/client/render/RenderTickCounter;FLnet/minecraft/client/gui/DrawContext;)V
    @Inject(
        //method = "renderStatusBars(Lnet/minecraft/client/render/RenderTickCounter;Lnet/minecraft/client/gui/DrawContext;)V", // Mantenha esta por enquanto se a abaixo der erro
        // TENTE ESTA SE A ACIMA FALHAR:
        method = "renderStatusBars(Lnet/minecraft/client/render/RenderTickCounter;FLnet/minecraft/client/gui/DrawContext;)V",
        at = @At("HEAD"),
        cancellable = true
    )
    //private void barium$onRenderStatusBars(RenderTickCounter tickCounter, DrawContext context, CallbackInfo ci) { // Removido float tickDelta para corresponder à assinatura mais provável
    // TENTE ESTA SE A ACIMA FALHAR:
    private void barium$onRenderStatusBars(RenderTickCounter tickCounter, float tickDelta, DrawContext context, CallbackInfo ci) { // Adicionado float tickDelta
        if (!HudOptimizer.shouldUpdateHudElement("status_bars", () -> "bars_placeholder_state")) {
            ci.cancel();
        }
    }
}