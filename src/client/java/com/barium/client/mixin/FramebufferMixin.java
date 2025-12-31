package com.barium.client.mixin;

import com.barium.config.BariumConfig;
import com.mojang.blaze3d.textures.FilterMode;
import net.minecraft.client.gl.Framebuffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Framebuffer.class)
public class FramebufferMixin {

    /**
     * Garante que, se o buffer for o nosso RetroBuffer (ou qualquer um em modo retro),
     * o filtro usado seja o NEAREST (Pixelado).
     */
    @ModifyVariable(
        method = "setFilter(Lcom/mojang/blaze3d/textures/FilterMode;Z)V", 
        at = @At("HEAD"), 
        argsOnly = true,
        ordinal = 0
    )
    private FilterMode barium$forceRetroFilter(FilterMode original) {
        if (BariumConfig.C.RENDER_SCALE_PERCENT < 100 && BariumConfig.C.USE_RETRO_FILTER) {
            // Nota: Isso afetará todos os framebuffers se o jogo tentar mudar o filtro
            // enquanto a config está ativa, mas é o comportamento desejado para o look "Retro".
            return FilterMode.NEAREST;
        }
        return original;
    }
}