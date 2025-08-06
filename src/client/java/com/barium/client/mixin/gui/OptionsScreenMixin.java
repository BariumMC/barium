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
public abstract class OptionsScreenMixin {

    /**
     * @author Barium (corrigido com base no Sodium e na assinatura da 1.21.8)
     * @reason Redireciona a criação da tela de Opções de Vídeo para a tela personalizada do Barium.
     * Esta é a abordagem final e correta, que respeita a assinatura exata do construtor.
     */
    @Redirect(
        method = "init",
        at = @At(
            value = "NEW",
            // O alvo é o construtor da VideoOptionsScreen.
            // O descriptor 'desc' é um argumento nomeado DENTRO da anotação @At.
            target = "net/minecraft/client/gui/screen/option/VideoOptionsScreen",
            // A anotação `args` é usada para especificar a assinatura exata do construtor,
            // resolvendo qualquer ambiguidade e garantindo que o mixin se aplique corretamente.
            args = {"Lnet/minecraft/client/gui/screen/Screen;", "Lnet/minecraft/client/MinecraftClient;", "Lnet/minecraft/client/option/GameOptions;"}
        )
    )
    private VideoOptionsScreen barium$redirectToCustomVideoSettings(Screen parent, MinecraftClient client, GameOptions gameOptions) {
        // Retornamos nossa classe anônima "falsa", passando adiante os 3 argumentos que capturamos.
        // O compilador agora ficará satisfeito, pois estamos chamando o construtor com os tipos corretos.
        return new VideoOptionsScreen(parent, client, gameOptions) {
            @Override
            public void init() {
                // Ao ser inicializada, a tela falsa se substitui pela nossa tela Barium Hub.
                this.client.setScreen(new BariumOptionsScreen(this.parent));
            }
        };
    }
}