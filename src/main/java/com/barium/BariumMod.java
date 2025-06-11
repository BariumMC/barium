// --- Edite o arquivo: src/main/java/com/barium/BariumMod.java ---
package com.barium;

import com.barium.config.ConfigManager;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BariumMod implements ModInitializer {
    public static final String MOD_ID = "barium";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Inicializando Barium...");
        
        // Carrega a configuração do arquivo assim que o mod é inicializado.
        ConfigManager.loadConfig();

        LOGGER.info("Barium inicializado com sucesso!");
    }
}