package com.barium.client.mixin;

import com.barium.client.optimization.SoundOptimizer;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundSystem;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SoundSystem.class)
public class SoundSystemMixin {

     static {
        System.out.println("MixinSound aplicado com sucesso!");
    }
    
    @Inject(method = "play", at = @At("HEAD"), cancellable = true)
    private void onPlay(SoundInstance sound, CallbackInfo ci) {
        // Obtém o jogador atual
        PlayerEntity player = null; // Precisaria obter o jogador do contexto
        
        // Verifica se o som deve ser reproduzido
        if (player != null && !SoundOptimizer.shouldPlaySound(sound, player)) {
            ci.cancel(); // Cancela a reprodução deste som
        }
    }
}
