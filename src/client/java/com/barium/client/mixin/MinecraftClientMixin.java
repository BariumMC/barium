package com.barium.client.mixin;

import com.barium.client.render.BariumRenderManager; // Criaremos esta classe a seguir
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {

    @Inject(method = "run", at = @At("HEAD"))
    private void barium$onStart(CallbackInfo ci) {
        // Inicializa nosso sistema de renderização quando o jogo começa.
        BariumRenderManager.getInstance().init();
    }
    
    @Inject(method = "stop", at = @At("HEAD"))
    private void barium$onShutdown(CallbackInfo ci) {
        // Libera os recursos (VBOs, etc.) quando o jogo fecha.
        BariumRenderManager.getInstance().shutdown();
    }
}