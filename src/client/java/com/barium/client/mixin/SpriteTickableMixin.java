package com.barium.client.mixin;

import com.barium.config.BariumConfig;
import net.minecraft.client.texture.Sprite;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Sprite.Tickable.class)
public class SpriteTickableMixin {

    /**
     * Injeta no método 'tick' das texturas animadas.
     * Se a opção estiver ativa, cancela a atualização da animação, "congelando" a textura.
     */
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void barium$freezeAnimatedTextures(CallbackInfo ci) {
        if (BariumConfig.C.DISABLE_TEXTURE_ANIMATIONS) {
            ci.cancel();
        }
    }
}