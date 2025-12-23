package com.barium.client.mixin;

import com.barium.BariumMod;
import com.barium.client.optimization.GuiRendererOptimizer;
import net.minecraft.client.gui.render.GuiRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.List;

@Mixin(GuiRenderer.class)
public abstract class GuiRendererMixin {

    @Shadow @Final private List<?> draws;

    /**
     * Otimização: Pré-processamento antes da renderização.
     */
    @Inject(method = "render", at = @At("HEAD"))
    private void barium$preRenderOptimize(CallbackInfo ci) {
        GuiRendererOptimizer.preRenderOptimize();
    }

    /**
     * Otimização: Pula renderização se otimização agressiva determinar.
     */
    @Inject(method = "renderPreparedDraws", at = @At("HEAD"), cancellable = true)
    private void barium$optimizeRenderPreparedDraws(CallbackInfo ci) {
        if (GuiRendererOptimizer.shouldSkipRenderPreparedDraws(draws.size(), draws)) {
            ci.cancel();
        }
    }

    /**
     * Otimização: Pós-processamento após render.
     */
    @Inject(method = "render", at = @At("TAIL"))
    private void barium$postRenderOptimize(CallbackInfo ci) {
        GuiRendererOptimizer.postRenderOptimize();
    }

    /**
     * Reseta o estado do otimizador por frame.
     */
    @Inject(method = "incrementFrame", at = @At("HEAD"))
    private void barium$resetOptimizer(CallbackInfo ci) {
        GuiRendererOptimizer.reset();
    }
}