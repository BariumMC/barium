package com.barium.client.mixin;

import com.barium.client.optimization.TickOptimizer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.SoundManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SoundManager.class)
public class SoundManagerMixin {

    @Inject(method = "tick(Z)V", at = @At("HEAD"), cancellable = true)
    private void barium$throttleBackgroundSoundTick(boolean paused, CallbackInfo ci) {
        if (paused) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (TickOptimizer.shouldSkipSoundManagerTick(client)) {
            ci.cancel();
        }
    }
}
