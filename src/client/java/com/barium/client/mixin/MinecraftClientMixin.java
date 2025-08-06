package com.barium.client.mixin;

import com.barium.client.optimization.EventLoopOptimizer;
import com.barium.config.BariumConfig;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {

    /**
     * Redireciona a chamada ao 'Thread.yield()' dentro do loop principal do jogo ('run' method).
     * Esta é a chamada que causa o "busy-waiting" e o alto uso de CPU.
     */
    @Redirect(
        method = "run()V",
        at = @At(
            value = "INVOKE",
            target = "Ljava/lang/Thread;yield()V"
        )
    )
    private void barium$replaceBusyWaitWithSleep() {
        // Se a nossa otimização estiver ativa, usamos nosso método de sleep.
        if (BariumConfig.C.ENABLE_SMART_SLEEP) {
            EventLoopOptimizer.smartYield();
        } else {
            // Caso contrário, mantemos o comportamento original do jogo.
            Thread.yield();
        }
    }
}