package com.barium.client;

import com.barium.BariumMod; // <-- IMPORTAÇÃO CORRIGIDA/ADICIONADA
import com.barium.client.optimization.HudOptimizer;
import com.barium.client.optimization.ParticleOptimizer;
import com.barium.client.optimization.ChunkOptimizer;
import com.barium.client.util.ChunkRenderManager; // <-- IMPORTAÇÃO ADICIONADA
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Environment(EnvType.CLIENT)
public class BariumClient implements ClientModInitializer {

    private static BariumClient instance;
    
    // A thread de trabalho para o culling de visibilidade
    public static final ExecutorService RENDER_THREAD_POOL = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "Barium Render Thread");
        t.setDaemon(true);
        return t;
    });

    // O ChunkRenderManager foi adicionado de volta
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

    // O método getter foi adicionado de volta para que os mixins possam usá-lo
    public ChunkRenderManager getChunkRenderManager() {
        return chunkRenderManager;
    }
}