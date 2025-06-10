package com.barium.client.mixin;

import com.barium.config.BariumConfig;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    // Usamos @Redirect para interceptar a chamada que cria ou redimensiona o framebuffer do contorno.
    @Redirect(
        method = "onResized",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gl/Framebuffer;resize(IIZ)V")
    )
    private void barium$resizeEntityOutlineFramebuffer(Framebuffer instance, int width, int height, boolean getError) {
        // Verificamos se esta é a instância correta do framebuffer (o do WorldRenderer)
        // e se a nossa otimização está ativa.
        GameRenderer gameRenderer = (GameRenderer)(Object)this;
        if (instance == gameRenderer.getWorldRenderer().getEntityOutlinesFramebuffer() && BariumConfig.ENABLE_HALF_RESOLUTION_ENTITY_OUTLINES) {
            // Se for, redimensionamos para metade da largura e altura.
            instance.resize(width / 2, height / 2, getError);
        } else {
            // Caso contrário, mantemos o comportamento original.
            instance.resize(width, height, getError);
        }
    }
}