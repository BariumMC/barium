package com.barium.client.mixin;

import com.barium.client.optimization.WeatherOptimizer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.WorldRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {

    // A INJEÇÃO ABAIXO FOI DESATIVADA (COMENTADA)
    // Para garantir a compatibilidade com Sodium e outros mods de performance (como ImmediatelyFast),
    // que reescrevem completamente o método de renderização do clima. Manter esta
    // injeção estava causando um crash por "target not found".
    /*
    @Inject(
        method = "renderWeather",
        at = @At(
            value = "INVOKE",
            target = "Ljava/util/Random;setSeed(J)V"
        ),
        cancellable = true
    )
    private void barium$reduceWeatherDensity(LightmapTextureManager manager, float tickDelta, double cameraX, double cameraY, double cameraZ, CallbackInfo ci) {
        if (!WeatherOptimizer.shouldRenderWeatherParticle()) {
            ci.cancel();
        }
    }
    */
}