package com.barium.client.mixin.gui;

import com.barium.client.config.BariumVideoSettingsScreen;
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
            // CORREÇÃO: O alvo agora inclui a assinatura completa do construtor com os 3 argumentos.
            target = "(Lnet/minecraft/client/gui/screen/Screen;Lnet/minecraft/client/MinecraftClient;Lnet/minecraft/client/option/GameOptions;)Lnet/minecraft/client/gui/screen/option/VideoOptionsScreen;"
        )
    )
    // CORREÇÃO: O método agora aceita os 3 argumentos que o Mixin captura do local da chamada original.
    private VideoOptionsScreen barium$redirectToCustomVideoSettings(Screen parent, MinecraftClient client, GameOptions gameOptions) {
        // Retornamos nossa classe anônima "falsa", passando adiante os 3 argumentos corretos.
        return new VideoOptionsScreen(parent, client, gameOptions) {
            @Override
            public void init() {
                // Ao ser inicializada, a tela falsa se substitui pela nossa tela Barium.
                this.client.setScreen(BariumVideoSettingsScreen.build(this.parent));
            }
        };
    }
}