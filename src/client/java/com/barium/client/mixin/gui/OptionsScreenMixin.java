package com.barium.client.mixin.gui;

import com.barium.client.config.BariumOptionsScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(OptionsScreen.class)
public class OptionsScreenMixin {

    /**
     * @author Barium (adaptado de Sodium)
     * @reason Redireciona o botão "Opções de Vídeo..." para a tela de configurações personalizada do Barium.
     * Esta é a abordagem mais estável e moderna, substituindo a tela inteira em vez de
     * tentar modificá-la.
     */
    @Redirect(
        method = "init",
        at = @At(
            value = "INVOKE",
            // Este é o alvo: o método que o botão "Opções de Vídeo..." chama para criar a tela.
            target = "Lnet/minecraft/client/gui/screen/option/OptionsScreen;openVideoOptionsScreen()V"
        )
    )
    private void redirectVideoOptionsScreen(OptionsScreen instance) {
        // Em vez de chamar o método original, nós simplesmente abrimos a nossa própria tela.
        Screen currentScreen = (Screen) (Object) this;
        currentScreen.getMinecraft().setScreen(new BariumOptionsScreen(currentScreen));
    }
}