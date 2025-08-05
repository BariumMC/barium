package com.barium.client.mixin;

import com.barium.config.BariumConfig;
import net.minecraft.client.texture.SpriteAtlasTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * CORREÇÃO FINAL: O alvo do Mixin agora é a classe correta, 'SpriteAtlasTexture'.
 * Este mixin desativa as animações de textura de forma estável e segura.
 */
@Mixin(SpriteAtlasTexture.class)
public class TextureAtlasMixin {

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void barium$freezeAllAnimatedTextures(CallbackInfo ci) {
        if (BariumConfig.C.DISABLE_TEXTURE_ANIMATIONS) {
            ci.cancel();
        }
    }
}