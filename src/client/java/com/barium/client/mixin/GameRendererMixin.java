package com.barium.client.mixin;

import com.barium.BariumMod;
import com.barium.client.optimization.CameraRotationTracker;
import com.barium.client.optimization.EntityOutlineOptimizer;
import com.barium.client.optimization.GuiRendererOptimizer;
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
        // ADIÇÃO: Atualiza o status da rotação no início do frame
        CameraRotationTracker.update();

        if (BariumConfig.C.ENABLE_AGGRESSIVE_OPTIMIZATION) {
            GuiRendererOptimizer.forceNextRender();
        }
    }
    /**
     * Otimização agressiva: Pós-render com métricas.
     */
    @Inject(method = "render", at = @At("TAIL"))
    private void barium$postRenderOptimize(CallbackInfo ci) {
        if (BariumConfig.C.ENABLE_AGGRESSIVE_OPTIMIZATION && BariumMod.LOGGER.isDebugEnabled()) {
            // Log adicional se necessário
        }
    }

    /**
     * Aplica o Downsampling Inteligente (estilo DLSS/FSR simples).
     * Redimensiona o buffer de outline para metade da resolução da tela.
     * Como o efeito é um "Blur", a perda visual é quase zero, mas o custo
     * de preenchimento de pixels (Fill Rate) cai em 75%.
     */
    @Inject(method = "onResized(II)V", at = @At("RETURN"))
    private void barium$forceResizeEntityOutlineFramebuffer(int width, int height, CallbackInfo ci) {
        Framebuffer entityOutlinesFramebuffer = MinecraftClient.getInstance().worldRenderer.getEntityOutlinesFramebuffer();
        
        if (entityOutlinesFramebuffer != null) {
            int divisor = EntityOutlineOptimizer.getResolutionDivisor();
            if (divisor > 1) {
                // Ex: Se a tela é 1920x1080, o buffer de brilho será 960x540.
                entityOutlinesFramebuffer.resize(width / divisor, height / divisor);
            }
        }
    }
}