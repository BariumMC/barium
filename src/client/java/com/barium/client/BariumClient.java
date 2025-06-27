package com.barium.client;

import com.barium.client.optimization.HudOptimizer;
import com.barium.client.optimization.ParticleOptimizer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Environment(EnvType.CLIENT)
public class BariumClient implements ClientModInitializer {

    private static BariumClient instance;
    
    // AQUI ESTÁ A NOSSA THREAD DE TRABALHO
    public static final ExecutorService RENDER_THREAD_POOL = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "Barium Render Thread");
        t.setDaemon(true); // Garante que a thread não impeça o jogo de fechar
        return t;
    });

    @Override
    public void onInitializeClient() {
        instance = this;
        BariumMod.LOGGER.info("Inicializando cliente Barium");
        
        HudOptimizer.init();
        ParticleOptimizer.init();
        // Não precisamos mais do ChunkOptimizer.init()
    }

    public static BariumClient getInstance() {
        return instance;
    }
}