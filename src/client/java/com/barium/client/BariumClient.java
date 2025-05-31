package com.barium.client;

import com.barium.BariumMod;
import com.barium.client.optimization.HudOptimizer;
import com.barium.client.optimization.ParticleOptimizer;
import com.barium.client.optimization.TransparentBlockOptimizer; // Importar nova classe
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class BariumClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        BariumMod.LOGGER.info("Inicializando cliente Barium");
        
        // Inicialização dos otimizadores client-side
        HudOptimizer.init();
        ParticleOptimizer.init();
        TransparentBlockOptimizer.init(); // Adicione esta linha
    }
}