package com.barium.client;

import com.barium.config.BariumConfig;
import me.shedaniel.autoconfig.AutoConfig;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.Screen; // Certifique-se de que este import está correto para sua versão do Minecraft

@Environment(EnvType.CLIENT)
public class BariumModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        // Retorna uma fábrica que cria a tela de configuração usando AutoConfig
        // AutoConfig.getConfigScreen(ConfigClass.class, parentScreen)
        // Isso criará a tela de configuração gerada automaticamente pelo Cloth Config para BariumConfig.
        return parent -> AutoConfig.getConfigScreen(BariumConfig.class, parent).build();
    }
}