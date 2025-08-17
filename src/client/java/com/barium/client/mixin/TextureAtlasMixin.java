package com.barium.client.mixin;

import com.barium.config.BariumConfig;
import net.minecraft.client.texture.SpriteAtlasTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin para desativar animações de textura e forçar um nível de mipmap.
 */
@Mixin(SpriteAtlasTexture.class)
public class TextureAtlasMixin {

    // Desativa animações de textura
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void barium$freezeAllAnimatedTextures(CallbackInfo ci) {
        if (BariumConfig.C.DISABLE_TEXTURE_ANIMATIONS) {
            ci.cancel();
        }
    }

    // Força o nível de mipmap
    @ModifyVariable(
        method = "upload(Lnet/minecraft/client/texture/SpriteAtlasTexture$Preparations;)V",
        at = @At("HEAD"),
        argsOnly = true
    )
    private SpriteAtlasTexture.Preparations barium$modifyMipmapLevel(SpriteAtlasTexture.Preparations preparations) {
        int override = BariumConfig.C.MIPMAP_LEVEL_OVERRIDE;
        if (override > 0) {
            // Recria o objeto Preparations com o novo nível de mipmap.
            // Isso é necessário porque o objeto original é um 'record' e imutável.
            return new SpriteAtlasTexture.Preparations(
                preparations.sprites(),
                preparations.width(),
                preparations.height(),
                Math.max(0, preparations.maxLevel() - override) // Reduz o nível máximo de mipmap
            );
        }
        return preparations;
    }
}