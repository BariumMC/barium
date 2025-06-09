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

    // Injetamos no método que renderiza cada "coluna" de chuva/neve
    @Inject(
        method = "renderWeather(Lnet/minecraft/client/render/LightmapTextureManager;FDDD)V",
        at = @At(
            value = "INVOKE",
            target = "Ljava/util/Random;setSeed(J)V" // Um ponto estável antes de renderizar a partícula
        ),
        cancellable = true
    )
    private void barium$reduceWeatherDensity(LightmapTextureManager manager, float tickDelta, double cameraX, double cameraY, double cameraZ, CallbackInfo ci) {
        if (!WeatherOptimizer.shouldRenderWeatherParticle()) {
            ci.cancel(); // Pula a renderização desta partícula de clima
        }
    }
}