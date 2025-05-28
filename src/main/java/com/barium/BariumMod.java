package com.barium;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// import com.barium.config.BariumConfig; // Config is usually accessed statically
// import com.barium.optimization.*; // Optimizers are typically used via Mixins or events, no direct init needed here

public class BariumMod implements ModInitializer {
    public static final String MOD_ID = "barium";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Inicializando Barium - Mod de otimização para Minecraft");

        // Inicializar sistemas de otimização server-side
        // Geralmente, não há necessidade de chamar métodos init() estáticos aqui.
        // A lógica dos otimizadores é ativada pelos Mixins ou por listeners de eventos.
        // PathfindingOptimizer.init(); // Remover
        // BlockTickOptimizer.init(); // Remover
        // RedstoneOptimizer.init(); // Remover
        // EntityTickOptimizer.init(); // Remover
        // InventoryOptimizer.init(); // Remover
        // ChunkSavingOptimizer.init(); // Remover

        // A inicialização pode envolver o registro de listeners de eventos, se necessário.
        // Exemplo: ServerTickEvents.END_SERVER_TICK.register(server -> { /* código */ });

        LOGGER.info("Barium inicializado com sucesso!");
    }
}

