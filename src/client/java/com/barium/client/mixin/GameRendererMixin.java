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

    // Usamos @Inject no final do método onResized. Esta é a abordagem mais segura.
    @Inject(
        method = "onResized(II)V",
        at = @At("RETURN")
    )
    private void barium$forceResizeEntityOutlineFramebuffer(int width, int height, CallbackInfo ci) {
        if (!BariumConfig.C.ENABLE_HALF_RESOLUTION_ENTITY_OUTLINES) {
            return;
        }

        // Obtemos a referência do framebuffer de contorno de forma segura.
        Framebuffer entityOutlinesFramebuffer = MinecraftClient.getInstance().worldRenderer.getEntityOutlinesFramebuffer();
        
        if (entityOutlinesFramebuffer != null) {
            // Forçamos o redimensionamento para metade da resolução da janela.
            // Isso sobrescreve qualquer redimensionamento que outro mod possa ter feito.
            entityOutlinesFramebuffer.resize(width / 2, height / 2);
        }
    }
}