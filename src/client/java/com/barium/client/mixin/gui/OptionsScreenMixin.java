package com.barium.client.mixin.gui;

import com.barium.client.config.BariumOptionsScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.option.GameOptions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(OptionsScreen.class)
public abstract class OptionsScreenMixin extends Screen {

    // Construtor necessário para satisfazer a herança da classe Screen.
    protected OptionsScreenMixin(net.minecraft.text.Text title) {
        super(title);
    }

    /**
     * @author Barium
     * @reason Modifica o botão "Opções de Vídeo..." para abrir a tela de configurações do Barium.
     * Esta abordagem usa @ModifyArg para substituir a ação do botão, sendo mais robusta
     * que @Redirect em `NEW` em ambientes complexos.
     */
    @ModifyArg(
        method = "init",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/screen/Screen;addDrawableChild(Lnet/minecraft/client/gui/widget/ClickableWidget;)Lnet/minecraft/client/gui/widget/ClickableWidget;"
        ),
        // O índice 0 refere-se ao primeiro argumento do método alvo, que é o widget a ser adicionado.
        index = 0
    )
    private ClickableWidget modifyVideoOptionsButton(ClickableWidget originalButton) {
        // Verificamos se o botão que está sendo adicionado é o de "Opções de Vídeo...".
        // Usamos a chave de tradução para uma comparação segura.
        if (originalButton instanceof ButtonWidget && originalButton.getMessage().equals(GameOptions.VIDEO_SETTINGS)) {
            // Se for o botão correto, nós criamos um *novo* botão para substituí-lo.
            // O novo botão tem a mesma aparência e posição, mas sua ação é abrir a nossa tela.
            return ButtonWidget.builder(GameOptions.VIDEO_SETTINGS, (button) -> {
                // Abre a tela de configurações do Barium.
                this.client.setScreen(new BariumOptionsScreen(this));
            }).dimensions(originalButton.getX(), originalButton.getY(), originalButton.getWidth(), originalButton.getHeight()).build();
        }

        // Se não for o botão de vídeo, nós o retornamos sem modificação.
        return originalButton;
    }
}