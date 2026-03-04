package com.barium.client.mixin;

import com.barium.client.optimization.CameraRotationTracker;
import com.barium.client.optimization.EntityOutlineOptimizer;
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
        // Atualiza o status da rotação no início do frame
        CameraRotationTracker.update();
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void barium$postRenderOptimize(CallbackInfo ci) {
        // Lógica pós-render, se necessária no futuro
    }

    /**
     * Aplica o Downsampling Inteligente (Reduz a resolução apenas do brilho/outline).
     */
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
