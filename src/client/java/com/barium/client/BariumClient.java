package com.barium.client;

import com.barium.BariumMod;
import com.barium.client.optimization.HudOptimizer;
import com.barium.client.optimization.ParticleOptimizer;
import com.barium.client.optimization.ChunkOptimizer;
import com.barium.client.util.ChunkRenderManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
// As importações de tick e de vetores não são mais necessárias aqui.

@Environment(EnvType.CLIENT)
public class BariumClient implements ClientModInitializer {

    private static BariumClient instance;
    private final ChunkRenderManager chunkRenderManager = new ChunkRenderManager();

    @Override
    public void onInitializeClient() {
        instance = this;
        BariumMod.LOGGER.info("Inicializando cliente Barium");
        
        HudOptimizer.init();
        ParticleOptimizer.init();
        ChunkOptimizer.init();

        // CORREÇÃO: A lógica de onClientTick foi removida completamente,
        // pois agora a atualização é feita pelo WorldRendererMixin, que é mais eficiente.
    }

    public static BariumClient getInstance() {
        return instance;
    }

    public ChunkRenderManager getChunkRenderManager() {
        return chunkRenderManager;
    }
}