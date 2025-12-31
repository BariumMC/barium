package com.barium.client.mixin;

import net.minecraft.client.gl.Framebuffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Framebuffer.class)
public interface FramebufferAccessor {
    // Acessa o ID do OpenGL (fbo) diretamente
    @Accessor("fbo")
    int getFbo();
}