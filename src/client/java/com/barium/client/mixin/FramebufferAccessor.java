package com.barium.client.mixin;

import net.minecraft.client.gl.Framebuffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Framebuffer.class)
public interface FramebufferAccessor {
    // Acessa o ID privado do OpenGL (fbo) para podermos fazer o bind manual
    @Accessor("fbo") // Se der erro de "fbo not found", tente @Accessor("index") ou @Accessor("a")
    int getFbo();
}