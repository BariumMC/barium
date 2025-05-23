package com.barium;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.barium.config.BariumConfig;
import com.barium.optimization.*;
import me.shedaniel.autoconfig.AutoConfig; // Importe para AutoConfig
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer; // Importe para o serializador

public class BariumMod implements ModInitializer {
    public static final String MOD_ID = "barium";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Inicializando Barium - Mod de otimização para Minecraft");
        
        // Registrar as configurações do Cloth Config
        BariumConfig.registerConfigs();
        
        // Inicializar sistemas de otimização server-side
        PathfindingOptimizer.init();
        BlockTickOptimizer.init();
        RedstoneOptimizer.init();
        EntityTickOptimizer.init();
        InventoryOptimizer.init();
        ChunkSavingOptimizer.init();
        ServerTerrainOptimizer.init(); 
        
        LOGGER.info("Barium inicializado com sucesso!");
    }
}