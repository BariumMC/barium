package com.barium.client.mixin;

import com.barium.client.optimization.EventLoopOptimizer;
import com.barium.config.BariumConfig;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = RenderSystem.class, remap = false)
public class RenderSystemMixin {

    /**
     * Intercepta a chamada ao limitador de framerate do Minecraft.
     * Se nossa otimização estiver ativa, nós a executamos e cancelamos o método original (o busy-wait).
     */
    @Inject(method = "limitFramerate", at = @At("HEAD"), cancellable = true)
    private static void barium$useSmartSleep(int framerate, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();

        // Só aplica a otimização se ela estiver ativada, o VSync estiver desligado e
        // a janela do jogo estiver em foco (para não limitar o FPS em segundo plano).
        if (BariumConfig.C.ENABLE_SMART_SLEEP && !client.options.getEnableVsync().getValue() && client.isWindowFocused()) {
            EventLoopOptimizer.waitForNextFrame();
            // Cancela o método vanilla para impedir o loop de espera ocupada.
            ci.cancel();
        }
    }
}