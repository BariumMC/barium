// src/client/java/com/barium/client/mixin/GameRendererMixin.java
package com.barium.client.mixin;

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

    @Inject(
        method = "onResized(II)V",
        at = @At("RETURN")
    )
    private void barium$forceResizeEntityOutlineFramebuffer(int width, int height, CallbackInfo ci) {
        if (BariumConfig.C.ENABLE_HALF_RESOLUTION_ENTITY_OUTLINES) {
            Framebuffer entityOutlinesFramebuffer = MinecraftClient.getInstance().worldRenderer.getEntityOutlinesFramebuffer();
            
            if (entityOutlinesFramebuffer != null) {
                // CORREÇÃO: A assinatura do método 'resize' em 1.21.8 aceita apenas 2 argumentos (width, height).
                entityOutlinesFramebuffer.resize(width / 2, height / 2);
            }
        }
    }
}