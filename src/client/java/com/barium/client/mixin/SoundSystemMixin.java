package com.barium.client.mixin;

import com.barium.client.optimization.SoundOptimizer;
import net.minecraft.client.MinecraftClient; // Import MinecraftClient
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundSystem;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin para a classe SoundSystem para otimizar a reprodução de sons.
 * Baseado nos mappings Yarn 1.21.5+build.1
 */
@Mixin(SoundSystem.class)
public abstract class SoundSystemMixin { // Made abstract as it doesn't implement SoundSystem methods

    /**
     * Injeta no início do método play() do SoundSystem.
     * Verifica se o som deve ser reproduzido com base na distância e obstruções.
     *
     * Target Method Signature (Yarn 1.21.5): Lnet/minecraft/client/sound/SoundSystem;play(Lnet/minecraft/client/sound/SoundInstance;)V
     */
    @Inject(
        method = "play(Lnet/minecraft/client/sound/SoundInstance;)V", // Assuming signature is correct for Yarn 1.21.5
        at = @At("HEAD"),
        cancellable = true
    )
    private void barium$onPlay(SoundInstance sound, CallbackInfo ci) {
        // Obtém o jogador atual do MinecraftClient
        PlayerEntity player = MinecraftClient.getInstance().player;

        // Verifica se o som deve ser reproduzido pelo otimizador
        // A lógica do SoundOptimizer precisa ser robusta e lidar com player == null (ex: menu principal)
        if (player != null && !SoundOptimizer.shouldPlaySound(sound, player)) {
            // Cancela a reprodução do som se o otimizador determinar que não é necessário
            ci.cancel();
        }
        // Se player for null ou shouldPlaySound retornar true, a reprodução continua normalmente.
    }
}

