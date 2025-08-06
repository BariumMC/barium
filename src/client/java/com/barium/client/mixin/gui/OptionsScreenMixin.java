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
     * Esta é a abordagem final e correta, que respeita a assinatura do construtor da 1.21.8.
     */
    @Redirect(
        method = "init",
        at = @At(
            value = "NEW",
            // O alvo é o construtor da VideoOptionsScreen.
            target = "net/minecraft/client/gui/screen/option/VideoOptionsScreen"
        ),
        // A anotação `desc` é usada para especificar a assinatura exata do construtor,
        // resolvendo qualquer ambiguidade e garantindo que o mixin se aplique corretamente.
        // (LScreen;LMinecraftClient;LGameOptions;)V significa: um construtor que aceita (Screen, MinecraftClient, GameOptions) e não retorna nada (V de void).
        remap = false // Desativamos o remapeamento para esta assinatura específica para garantir a estabilidade.
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