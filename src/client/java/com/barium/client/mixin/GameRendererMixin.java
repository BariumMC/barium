package com.barium.client.mixin;

import com.barium.client.optimization.CameraRotationTracker;
import com.barium.client.optimization.EntityOutlineOptimizer;
import com.barium.client.optimization.GuiRendererOptimizer;
import com.barium.client.util.RetroBufferManager;
import com.barium.config.BariumConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    @Inject(method = "render", at = @At("HEAD"))
    private void barium$preRenderOptimize(CallbackInfo ci) {
        CameraRotationTracker.update();

        if (BariumConfig.C.ENABLE_AGGRESSIVE_OPTIMIZATION) {
            GuiRendererOptimizer.forceNextRender();
        }
    }

    // --- LÓGICA RETRO START ---
    
    // Antes de renderizar o mundo (World + Hand), trocamos para o buffer pequeno
    @Inject(method = "renderWorld", at = @At("HEAD"))
    private void barium$startRetroRender(CallbackInfo ci) {
        if (RetroBufferManager.isActive()) {
            RetroBufferManager.beginRender(MinecraftClient.getInstance());
        }
    }

    // Depois de renderizar o mundo, pegamos o buffer pequeno e desenhamos grande na tela
    @Inject(method = "renderWorld", at = @At("TAIL"))
    private void barium$endRetroRender(CallbackInfo ci) {
        if (RetroBufferManager.isActive()) {
            RetroBufferManager.endRenderAndBlit(MinecraftClient.getInstance());
        }
    }
    
    @Inject(method = "onResized", at = @At("HEAD"))
    private void barium$onResized(int width, int height, CallbackInfo ci) {
        RetroBufferManager.resize();
    }

    // --- LÓGICA RETRO END ---

    @Inject(method = "onResized(II)V", at = @At("RETURN"))
    private void barium$forceResizeEntityOutlineFramebuffer(int width, int height, CallbackInfo ci) {
        Framebuffer entityOutlinesFramebuffer = MinecraftClient.getInstance().worldRenderer.getEntityOutlinesFramebuffer();
        if (entityOutlinesFramebuffer != null) {
            int divisor = EntityOutlineOptimizer.getResolutionDivisor();
            if (divisor > 1) {
                entityOutlinesFramebuffer.resize(width / divisor, height / divisor);
            }
        }
    }
}