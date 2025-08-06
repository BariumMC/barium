package com.barium.client.mixin.gui;

import com.barium.client.config.BariumVideoSettingsScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(OptionsScreen.class)
public class OptionsScreenMixin {

    /**
     * Intercepta o método que cria a tela de opções de vídeo.
     * Em vez de deixar o método original rodar, nós o cancelamos e retornamos
     * a nossa própria tela de configurações do Barium.
     * Esta abordagem é mais estável do que redirecionar o 'new'.
     */
    @Inject(
        method = "createVideoOptionsScreen",
        at = @At("HEAD"),
        cancellable = true
    )
    private void barium$redirectToCustomVideoSettings(CallbackInfoReturnable<Screen> cir) {
        // Pega a tela atual (OptionsScreen) para usá-la como tela "pai".
        Screen parent = (Screen) (Object) this;
        // Define o valor de retorno do método como a nossa tela Barium.
        cir.setReturnValue(BariumVideoSettingsScreen.build(parent));
        // A chamada a setReturnValue automaticamente cancela o resto do método original.
    }
}