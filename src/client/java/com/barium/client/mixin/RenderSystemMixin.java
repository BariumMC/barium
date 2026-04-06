package com.barium.client.mixin;

import com.barium.client.optimization.EventPollingOptimizer;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderSystem.class)
public class RenderSystemMixin {
    @Inject(method = "pollEvents", at = @At("HEAD"), cancellable = true)
    private static void barium$throttlePollEventsWhenUnfocused(CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (EventPollingOptimizer.shouldSkipPollEvents(client)) {
            ci.cancel();
        }
    }

    @Inject(method = "pollEvents", at = @At("TAIL"))
    private static void barium$markPollEventsExecution(CallbackInfo ci) {
        EventPollingOptimizer.markPollExecuted();
    }
}
