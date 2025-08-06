package com.barium.client.mixin.gui;

import com.barium.client.config.BariumVideoSettingsScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.screen.option.VideoOptionsScreen;
import net.minecraft.client.option.GameOptions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(OptionsScreen.class)
public class OptionsScreenMixin {

    /**
     * @author Barium
     * @reason Redireciona a criação da tela de Opções de Vídeo para a tela personalizada do Barium.
     * Esta é a abordagem mais estável e correta.
     */
    @Redirect(
        method = "init",
        at = @At(
            value = "NEW",
            // O alvo é o construtor da VideoOptionsScreen. Esta assinatura é a chave para o sucesso.
            target = "net/minecraft/client/gui/screen/option/VideoOptionsScreen"
        )
    )
    private VideoOptionsScreen barium$redirectToCustomVideoSettings(Screen parent, GameOptions gameOptions) {
        // Retornamos uma classe anônima "falsa" que, ao ser inicializada,
        // imediatamente se substitui pela nossa tela Barium.
        return new VideoOptionsScreen(parent, gameOptions) {
            @Override
            public void init() {
                this.client.setScreen(BariumVideoSettingsScreen.build(this.parent));
            }
        };
    }
}