package com.barium;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import me.shedaniel.autoconfig.AutoConfig; // Importe para AutoConfig
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer; // Importe para o serializador
import com.barium.config.BariumConfig; // Importe sua classe de configuração

public class BariumMod implements ModInitializer {
    public static final String MOD_ID = "barium";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Inicializando Barium - Mod de otimização para Minecraft");

        // Registrar as configurações do Cloth Config
        AutoConfig.register(BariumConfig.class, JanksonConfigSerializer::new); // Esta linha é crucial!

        LOGGER.info("Barium inicializado com sucesso!");
    }
}