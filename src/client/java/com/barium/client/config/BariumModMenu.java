package com.barium.client.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

public class BariumModMenu implements ModMenuApi {

    /**
     * Quando o botão de config do Barium for clicado no ModMenu,
     * ele agora abrirá nossa nova tela de configurações de vídeo unificada.
     */
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> BariumVideoSettingsScreen.build(parent);
    }
}