package com.barium.client.mixin.gui;

import com.barium.client.config.BariumVideoSettingsScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.GameOptionsScreen;
import net.minecraft.client.option.GameOptions;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

// CORREÇÃO: O alvo correto do mixin é GameOptionsScreen, não OptionsScreen.
@Mixin(GameOptionsScreen.class)
public abstract class GameOptionsScreenMixin extends Screen {

    @Shadow @Final protected GameOptions gameOptions;
    @Shadow @Final protected Screen parent;

    // Construtor necessário para estender a Screen
    protected GameOptionsScreenMixin(Screen parent, GameOptions gameOptions, net.minecraft.text.Text title) {
        super(title);
        this.parent = parent;
        this.gameOptions = gameOptions;
    }

    /**
     * @author Barium
     * @reason Substitui completamente a tela de opções de vídeo vanilla pela tela unificada do Barium.
     * A anotação @Overwrite é a maneira mais robusta de substituir um método inteiro.
     * Agora que estamos na classe correta, esta injeção funcionará.
     */
    @Overwrite
    protected Screen createVideoOptionsScreen() {
        return BariumVideoSettingsScreen.build(this.parent);
    }
}