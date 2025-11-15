package com.barium.client.mixin;

import com.barium.config.BariumConfig;
import net.minecraft.client.texture.SpriteAtlasTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * This mixin now ONLY handles disabling texture animations, as the mipmap logic
 * has been moved to SpriteLoaderMixin for correctness and stability.
 */
@Mixin(SpriteAtlasTexture.class)
public class TextureAtlasMixin {

    @Inject(method = "tick()V", at = @At("HEAD"), cancellable = true)
    private void barium$freezeAllAnimatedTextures(CallbackInfo ci) {
        if (BariumConfig.C.DISABLE_TEXTURE_ANIMATIONS) {
            ci.cancel();
        }
    }
}