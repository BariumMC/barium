package com.barium.client.mixin.gui;

import com.barium.client.config.BariumVideoSettingsScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.screen.option.VideoOptionsScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

// O alvo correto é a OptionsScreen, onde o botão de Opções de Vídeo é criado.
@Mixin(OptionsScreen.class)
public class OptionsScreenMixin {

    /**
     * Redireciona a ação do botão "Opções de Vídeo...".
     * Em vez de deixar o jogo abrir a tela de vídeo padrão, nós interceptamos a chamada
     * e a substituímos pela nossa tela personalizada do Barium.
     *
     * @param client A instância do MinecraftClient.
     * @param screen A tela que seria aberta (nós vamos verificar se é a VideoOptionsScreen).
     */
    @Redirect(
        method = "init", // O método onde os botões são inicializados.
        at = @At(
            value = "INVOKE",
            // O alvo é a chamada para client.setScreen() que abre a nova tela.
            target = "Lnet/minecraft/client/MinecraftClient;setScreen(Lnet/minecraft/client/gui/screen/Screen;)V"
        )
    )
    private void barium$redirectVideoSettingsButton(MinecraftClient client, Screen screen) {
        // Verificação de segurança CRUCIAL: só substituímos se a tela for a de vídeo.
        // Isso impede que a gente quebre outros botões, como o de "Controles...".
        if (screen instanceof VideoOptionsScreen) {
            // Se for, abrimos a nossa tela, passando a tela atual como "pai".
            client.setScreen(BariumVideoSettingsScreen.build((Screen)(Object)this));
        } else {
            // Se não for, deixamos o comportamento original acontecer.
            client.setScreen(screen);
        }
    }
}