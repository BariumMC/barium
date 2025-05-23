package com.barium.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.autoconfig.AutoConfig;

/**
 * Integração com o ModMenu para permitir acesso ao menu de configurações do Barium.
 */
public class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getConfigScreenFactory() {
        // Retorna uma fábrica de tela de configuração que usa o AutoConfig para construir a GUI.
        return parent -> AutoConfig.getConfigScreen(BariumConfig.class, parent).build();
    }
}