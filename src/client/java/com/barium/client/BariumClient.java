package com.barium.client;

import com.barium.BariumMod;
import com.barium.client.optimization.HudOptimizer;
import com.barium.client.optimization.ParticleOptimizer;
import com.barium.client.optimization.ChunkOptimizer;
import com.barium.client.util.ChunkRenderManager; // ADICIONE ESTE IMPORT
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class BariumClient implements ClientModInitializer {

    private static BariumClient instance;
    // ADICIONE ESTA LINHA
    private final ChunkRenderManager chunkRenderManager = new ChunkRenderManager();

    @Override
    public void onInitializeClient() {
        instance = this;
        BariumMod.LOGGER.info("Inicializando cliente Barium");
        
        HudOptimizer.init();
        ParticleOptimizer.init();
        ChunkOptimizer.init();
    }

    public static BariumClient getInstance() {
        return instance;
    }

    // ADICIONE ESTE MÉTODO
    public ChunkRenderManager getChunkRenderManager() {
        return chunkRenderManager;
    }
}