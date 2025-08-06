package com.barium.client.mixin.gui;

import com.barium.client.config.BariumVideoSettingsScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.screen.option.VideoOptionsScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(OptionsScreen.class)
public class OptionsScreenMixin {

    /**
     * Redireciona a criação da tela de Opções de Vídeo.
     * Em vez de criar a tela vanilla (new VideoOptionsScreen),
     * nós chamamos nosso método que constrói a tela personalizada do Barium.
     */
    @Redirect(
        method = "createVideoOptionsScreen",
        at = @At(
            value = "NEW",
            target = "Lnet/minecraft/client/gui/screen/option/VideoOptionsScreen;"
        )
    )
    private VideoOptionsScreen barium$redirectToCustomVideoSettings(Screen parent) {
        // Retorna um wrapper da nossa tela, pois o método espera um VideoOptionsScreen.
        // Isso é uma pequena gambiarra para fazer a substituição funcionar.
        // Criamos uma classe anônima que estende VideoOptionsScreen mas que na verdade mostra a nossa tela.
        return new VideoOptionsScreen(parent, MinecraftClient.getInstance().options) {
            @Override
            public void init() {
                // Quando esta tela for inicializada, trocamos ela pela nossa tela Barium.
                this.client.setScreen(BariumVideoSettingsScreen.build(parent));
            }
        };
    }
}