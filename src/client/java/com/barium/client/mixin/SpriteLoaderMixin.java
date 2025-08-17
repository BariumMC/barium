package com.barium.client.mixin;

import com.barium.config.BariumConfig;
import net.minecraft.client.texture.SpriteLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.List;
import java.util.concurrent.Executor;

@Mixin(SpriteLoader.class)
public class SpriteLoaderMixin {

    /**
     * Injects into the 'stitch' method to modify the mipmap level before textures are processed.
     * This is the correct, modern location for this logic.
     *
     * @param mipLevel The original mipmap level from game settings.
     * @return The potentially modified mipmap level.
     */
    @ModifyVariable(
        method = "stitch(Ljava/util/List;ILjava/util/concurrent/Executor;)Lnet/minecraft/client/texture/SpriteLoader$StitchResult;",
        at = @At("HEAD"),
        argsOnly = true,
        ordinal = 0 // The first integer argument is the mipLevel
    )
    private int barium$modifyMipmapLevel(int mipLevel) {
        int override = BariumConfig.C.MIPMAP_LEVEL_OVERRIDE;

        if (override > 0) {
            // Subtract our override from the user's current mipmap setting.
            // Math.max(0, ...) ensures we don't go below zero.
            return Math.max(0, mipLevel - override);
        }

        // If our override is off, return the original value.
        return mipLevel;
    }
}