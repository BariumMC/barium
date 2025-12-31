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
     * Intercepta o parâmetro 'filter' no método 'setFilter'.
     * Se o modo Retrô estiver ativado, forçamos o filtro para NEAREST (Pixelado).
     */
    @ModifyVariable(
        method = "setFilter(Lcom/mojang/blaze3d/textures/FilterMode;Z)V", 
        at = @At("HEAD"), 
        argsOnly = true,
        ordinal = 0 // O primeiro argumento (FilterMode)
    )
    private FilterMode barium$forceRetroFilter(FilterMode original) {
        // Se a escala estiver reduzida (< 100%) e o filtro Retrô estiver ligado:
        if (BariumConfig.C.RENDER_SCALE_PERCENT < 100 && BariumConfig.C.USE_RETRO_FILTER) {
            return FilterMode.NEAREST; // Retorna "Pixelado"
        }
        
        // Caso contrário, deixa o filtro original (que costuma ser LINEAR/Borrado ou o que o jogo pediu)
        return original;
    }
}