package com.barium.client.mixin;

import com.barium.client.optimization.EventPollingOptimizer;
import com.barium.config.BariumConfig;
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
        if (!BariumConfig.C.ENABLE_BACKGROUND_EVENT_THROTTLING) return;
        EventPollingOptimizer.markPollExecuted();
    }
}
