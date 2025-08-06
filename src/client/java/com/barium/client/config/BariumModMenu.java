package com.barium.client.mixin.gui;

import com.barium.client.config.BariumVideoSettingsScreen;
import net.minecraft.client.MinecraftClient; // <-- IMPORT ADICIONADO
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.screen.option.VideoOptionsScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(OptionsScreen.class)
public class OptionsScreenMixin {

    /**
     * Redireciona a criação da tela de Opções de Vídeo.
     */
    @Redirect(
        method = "createVideoOptionsScreen",
        at = @At(
            value = "NEW",
            target = "Lnet/minecraft/client/gui/screen/option/VideoOptionsScreen;"
        )
    )
    private VideoOptionsScreen barium$redirectToCustomVideoSettings(Screen parent, net.minecraft.client.option.GameOptions options) {
        // Quando o jogo tenta criar 'new VideoOptionsScreen(...)', nós interceptamos
        // e, em vez disso, retornamos uma classe "falsa" que, ao ser aberta,
        // imediatamente se substitui pela nossa tela personalizada do Barium.
        return new VideoOptionsScreen(parent, options) {
            @Override
            public void init() {
                this.client.setScreen(BariumVideoSettingsScreen.build(this.parent));
            }
        };
    }
}