package com.barium.client.mixin;

import com.barium.config.BariumConfig;
import net.minecraft.client.util.Window;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Window.class)
public class WindowMixin {

    @Shadow private int framebufferWidth;
    @Shadow private int framebufferHeight;

    /**
     * Intercepta o momento em que o jogo define o tamanho do "desenho" (framebuffer).
     * Nós pegamos o tamanho real da janela e multiplicamos pela porcentagem (ex: 0.5).
     */
    @Inject(method = "onFramebufferSizeChanged", at = @At("RETURN"))
    private void barium$applyRenderScale(long window, int width, int height, CallbackInfo ci) {
        if (BariumConfig.C.RENDER_SCALE_PERCENT < 100) {
            float multiplier = BariumConfig.C.RENDER_SCALE_PERCENT / 100f;
            
            // Aplica a redução de resolução
            int newW = (int) (width * multiplier);
            int newH = (int) (height * multiplier);
            
            this.framebufferWidth = Math.max(1, newW);
            this.framebufferHeight = Math.max(1, newH);
        }
    }
}