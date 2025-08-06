package com.barium.client.mixin.gui;

import com.barium.client.config.BariumOptionsScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(OptionsScreen.class)
public abstract class OptionsScreenMixin extends Screen {

    // Adiciona um construtor para satisfazer o compilador, pois estamos estendendo Screen.
    protected OptionsScreenMixin(net.minecraft.text.Text title) {
        super(title);
    }

    /**
     * @author Barium (adaptado de Sodium)
     * @reason Redireciona o botão "Opções de Vídeo..." para a tela de configurações personalizada do Barium.
     * Esta é a abordagem mais estável, substituindo a ação do botão em vez de
     * depender de assinaturas de construtor frágeis.
     */
    @Redirect(
        method = "init",
        at = @At(
            value = "INVOKE",
            // Este é o alvo: o método que a lambda do botão "Opções de Vídeo..." chama.
            target = "Lnet/minecraft/client/gui/screen/option/OptionsScreen;openVideoOptionsScreen()V"
        )
    )
    private void redirectVideoOptionsScreen(OptionsScreen instance) {
        // Em vez de chamar o método original, nós simplesmente abrimos a nossa própria tela.
        // this.client está disponível porque a classe do Mixin estende Screen.
        this.client.setScreen(new BariumOptionsScreen(this));
    }
}