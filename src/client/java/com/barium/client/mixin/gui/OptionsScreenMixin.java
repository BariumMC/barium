package com.barium.client.mixin.gui;

import com.barium.client.config.BariumOptionsScreen;
import net.minecraft.client.MinecraftClient;
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
            // A assinatura correta do construtor na versão 1.21.8.
            target = "net/minecraft/client/gui/screen/option/VideoOptionsScreen",
            // Especifica a assinatura exata para evitar ambiguidades.
            desc = "(Lnet/minecraft/client/gui/screen/Screen;Lnet/minecraft/client/option/GameOptions;)V"
        )
    )
    private VideoOptionsScreen barium$redirectToCustomVideoSettings(Screen parent, GameOptions gameOptions) {
        // Retornamos uma classe anônima "falsa" que se substitui ao ser inicializada.
        return new VideoOptionsScreen(parent, gameOptions) {
            @Override
            public void init() {
                // Abre nossa tela "Hub" em vez da tela de vídeo vanilla.
                this.client.setScreen(new BariumOptionsScreen(this.parent));
            }
        };
    }
}