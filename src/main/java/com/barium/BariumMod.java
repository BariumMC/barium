package com.barium;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.barium.config.BariumConfig;
import com.barium.optimization.*;

public class BariumMod implements ModInitializer {
    public static final String MOD_ID = "barium";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Inicializando Barium - Mod de otimização para Minecraft");
        
        // Inicializar sistemas de otimização server-side
        PathfindingOptimizer.init();
        BlockTickOptimizer.init();
        RedstoneOptimizer.init();
        EntityTickOptimizer.init();
        InventoryOptimizer.init();
        ChunkSavingOptimizer.init();
        ServerTerrainOptimizer.init(); // Initialize the server terrain optimizer
        
        LOGGER.info("Barium inicializado com sucesso!");
    }
}