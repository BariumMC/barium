package com.barium.client.mixin;

import com.barium.client.optimization.HudOptimizer;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {

    /**
     * Injeta no método disconnect() do MinecraftClient para limpar o cache da HUD.
     * Isso garante que os dados da HUD não persistam entre as sessões de jogo/mundos.
     *
     * @param ci O CallbackInfo.
     */
    @Inject(method = "disconnect", at = @At("HEAD"))
    private void barium$onDisconnect(CallbackInfo ci) {
        HudOptimizer.clearHudCache();
    }
}