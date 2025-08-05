package com.barium.client.mixin;

import com.barium.config.BariumConfig;
import net.minecraft.client.texture.TextureAtlas;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Este mixin substitui o antigo SpriteTickableMixin.
 * Ele desativa as animações de textura de uma forma muito mais estável e segura,
 * interceptando o método que executa a atualização de todas as texturas animadas.
 */
@Mixin(TextureAtlas.class)
public class TextureAtlasMixin {

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void barium$freezeAllAnimatedTextures(CallbackInfo ci) {
        if (BariumConfig.C.DISABLE_TEXTURE_ANIMATIONS) {
            ci.cancel();
        }
    }
}