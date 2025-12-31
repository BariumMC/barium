package com.barium.client.mixin;

import com.barium.config.BariumConfig;
import com.mojang.blaze3d.opengl.GlStateManager;
import net.minecraft.client.gl.Framebuffer;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Framebuffer.class)
public class FramebufferMixin {

    @Inject(method = "setTexFilter", at = @At("HEAD"), cancellable = true)
    private void barium$forceRetroFilter(int filter, CallbackInfo ci) {
        // Se a escala estiver reduzida e o usuário quiser o visual "Retro" (Pixelado)
        if (BariumConfig.C.RENDER_SCALE_PERCENT < 100 && BariumConfig.C.USE_RETRO_FILTER) {
            // Força o filtro GL_NEAREST (Pixelado)
            GlStateManager._texParameter(3553, 10241, GL11.GL_NEAREST);
            GlStateManager._texParameter(3553, 10240, GL11.GL_NEAREST);
            ci.cancel(); // Impede o jogo de colocar o filtro padrão (Linear/Borrado)
        }
    }
}