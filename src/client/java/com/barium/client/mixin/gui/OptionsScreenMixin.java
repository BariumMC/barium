package com.barium.client.mixin.gui;

import com.barium.client.config.BariumOptionsScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.option.GameOptions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(OptionsScreen.class)
public abstract class OptionsScreenMixin extends Screen {

    // Construtor necessário para a herança
    protected OptionsScreenMixin(net.minecraft.text.Text title) {
        super(title);
    }

    /**
     * @author Barium
     * @reason Redireciona a criação do botão "Opções de Vídeo..." para um novo botão
     * que abre a tela de configurações do Barium. Esta é uma abordagem robusta
     * que intercepta a criação do widget específico.
     */
    @Redirect(
        method = "init",
        at = @At(
            value = "INVOKE",
            // O alvo é a criação do botão (ButtonWidget.builder) que tem como texto
            // o resultado de GameOptions.getVideoSettings().
            // A gente intercepta a chamada ao construtor do botão e o substituímos.
            target = "Lnet/minecraft/client/option/GameOptions;getVideoSettings()Lnet/minecraft/text/Text;"
        )
    )
    private net.minecraft.text.Text getVideoSettingsButtonText(GameOptions instance) {
        // Este método é apenas um ponto de ancoragem para o próximo Redirect.
        // Retornamos o valor original para não quebrar nada.
        return instance.getVideoSettings();
    }

    @Redirect(
        method = "init",
        at = @At(
            value = "INVOKE",
            // Agora redirecionamos a criação do botão que USA o texto que interceptamos acima.
            target = "Lnet/minecraft/client/gui/widget/ButtonWidget;builder(Lnet/minecraft/text/Text;Lnet/minecraft/client/gui/widget/ButtonWidget$PressAction;)Lnet/minecraft/client/gui/widget/ButtonWidget$Builder;"
        )
    )
    private ButtonWidget.Builder redirectVideoSettingsButton(net.minecraft.text.Text text, ButtonWidget.PressAction onPress) {
        // Verificamos se o texto do botão é o de "Opções de Vídeo".
        if (text.equals(this.client.options.getVideoSettings())) {
            // Se for, criamos um novo builder de botão com a NOSSA ação.
            return ButtonWidget.builder(text, (button) -> {
                this.client.setScreen(new BariumOptionsScreen(this));
            });
        }
        // Se não for, retornamos o builder original.
        return ButtonWidget.builder(text, onPress);
    }
}