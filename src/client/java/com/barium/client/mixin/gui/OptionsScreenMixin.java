package com.barium.client.mixin.gui;

import com.barium.client.config.BariumVideoSettingsScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(OptionsScreen.class)
public class OptionsScreenMixin {

    @Shadow @Final private Screen parent;

    /**
     * @author Barium
     * @reason Substitui completamente a tela de opções de vídeo vanilla pela tela unificada do Barium.
     * A anotação @Overwrite é a maneira mais robusta de substituir um método inteiro.
     */
    @Overwrite
    public Screen createVideoOptionsScreen() {
        // Retorna a nossa tela personalizada, construída com a tela "pai" correta.
        return BariumVideoSettingsScreen.build(this.parent);
    }
}