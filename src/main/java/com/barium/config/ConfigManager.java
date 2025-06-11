// --- Crie este arquivo em: src/main/java/com/barium/config/ConfigManager.java ---
package com.barium.config;

import com.barium.BariumMod;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

public class ConfigManager {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_FILE_PATH = FabricLoader.getInstance().getConfigDir().resolve("barium.json");

    /**
     * Carrega as configurações do arquivo. Se o arquivo não existir, cria um novo com os valores padrão.
     */
    public static void loadConfig() {
        try {
            if (CONFIG_FILE_PATH.toFile().exists()) {
                try (FileReader reader = new FileReader(CONFIG_FILE_PATH.toFile())) {
                    BariumConfig.C = GSON.fromJson(reader, ConfigData.class);
                    // Garante que se uma nova config for adicionada ao mod, o arquivo seja atualizado.
                    if (BariumConfig.C == null) {
                        BariumConfig.C = new ConfigData();
                    }
                    BariumMod.LOGGER.info("Configurações do Barium carregadas de " + CONFIG_FILE_PATH);
                }
            }
            // Após carregar (ou se não existia), salva para criar o arquivo ou adicionar novas opções.
            saveConfig();
        } catch (IOException e) {
            BariumMod.LOGGER.error("Falha ao carregar a configuração do Barium!", e);
        }
    }

    /**
     * Salva as configurações atuais no arquivo.
     */
    public static void saveConfig() {
        try (FileWriter writer = new FileWriter(CONFIG_FILE_PATH.toFile())) {
            GSON.toJson(BariumConfig.C, writer);
        } catch (IOException e) {
            BariumMod.LOGGER.error("Falha ao salvar a configuração do Barium!", e);
        }
    }
}