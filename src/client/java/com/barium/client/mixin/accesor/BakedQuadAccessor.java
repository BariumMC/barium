package com.barium.client.mixin.accesor;

import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.texture.Sprite;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BakedQuad.class)
public interface BakedQuadAccessor {
    // Permite acessar o Sprite associado a uma BakedQuad
    @Accessor("sprite")
    Sprite getSprite();
}