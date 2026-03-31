package com.barium.client.mixin;

import com.barium.client.optimization.HandRenderOptimizer;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class HandRenderMixin {

    @Inject(method = "renderHand", at = @At("HEAD"), cancellable = true)
    private void barium$skipHandRenderWhenSafe(CallbackInfo ci) {
        if (HandRenderOptimizer.shouldSkipHandRender()) {
            ci.cancel();
        }
    }
}
