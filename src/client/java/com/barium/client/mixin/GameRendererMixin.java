package com.barium.client.mixin;

import com.barium.config.BariumConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    @Redirect(
        method = "onResized(II)V",
        at = @At(
            value = "INVOKE",
            // O alvo é a chamada para redimensionar o framebuffer principal do WorldRenderer
            target = "Lnet/minecraft/client/gl/Framebuffer;resize(II)V"
        )
    )
    private void barium$resizeEntityOutlineFramebuffer(Framebuffer instance, int width, int height) {
        // CORREÇÃO: Obtemos a referência do framebuffer de contorno diretamente do WorldRenderer.
        Framebuffer entityOutlinesFramebuffer = MinecraftClient.getInstance().worldRenderer.getEntityOutlinesFramebuffer();

        // Se a instância que estamos interceptando é o framebuffer de contorno...
        if (instance == entityOutlinesFramebuffer && BariumConfig.ENABLE_HALF_RESOLUTION_ENTITY_OUTLINES) {
            // ...redimensionamos para metade da resolução.
            instance.resize(width / 2, height / 2);
        } else {
            // Caso contrário, usamos o comportamento original.
            instance.resize(width, height);
        }
    }
}