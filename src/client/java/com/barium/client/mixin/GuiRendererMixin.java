package com.barium.client.mixin;

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

    @Inject(method = "render", at = @At("HEAD"))
    private void barium$preRenderOptimize(CallbackInfo ci) {
        GuiRendererOptimizer.preRenderOptimize();
    }

    /**
     * Otimização Crítica: renderPreparedDraws consome muita CPU iterando listas.
     * Se o otimizador detectar que a tela está estática, cancelamos totalmente.
     */
    @Inject(method = "renderPreparedDraws", at = @At("HEAD"), cancellable = true)
    private void barium$optimizeRenderPreparedDraws(CallbackInfo ci) {
        // Passa apenas o tamanho (O(1)) para evitar verificações profundas na lista
        if (GuiRendererOptimizer.shouldSkipRenderPreparedDraws(draws.size())) {
            ci.cancel();
        }
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void barium$postRenderOptimize(CallbackInfo ci) {
        GuiRendererOptimizer.postRenderOptimize();
    }
}