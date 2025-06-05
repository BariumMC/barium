package com.barium.client;

import com.barium.BariumMod;
import com.barium.client.optimization.HudOptimizer;
import com.barium.client.optimization.ParticleOptimizer;
import com.barium.client.optimization.ChunkOptimizer;
import com.barium.client.util.ChunkRenderManager;
import com.barium.client.optimization.resource.AsyncResourceLoader; // Importar AsyncResourceLoader
import com.barium.client.optimization.gui.GuiOptimizer; // Importar GuiOptimizer

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents; // Importar ClientLifecycleEvents

@Environment(EnvType.CLIENT)
public class BariumClient implements ClientModInitializer {

    // Instância do ChunkRenderManager para gerenciar o estado e os cálculos
    private ChunkRenderManager chunkRenderManager;

    @Override
    public void onInitializeClient() {
        BariumMod.LOGGER.info("Inicializando cliente Barium");
        
        // Inicialização dos otimizadores client-side
        HudOptimizer.init();
        ParticleOptimizer.init();
        ChunkOptimizer.init();
        GuiOptimizer.init(); // Inicializa GuiOptimizer
        AsyncResourceLoader.init(); // Inicializa AsyncResourceLoader

        // Inicializa o ChunkRenderManager
        chunkRenderManager = new ChunkRenderManager();

        // Registra um listener para o final de cada tick do cliente
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player != null) {
                // Verifica se é necessário recalcular os chunks a serem renderizados
                if (chunkRenderManager.shouldRecalculate(client.player.getPos(), client.player.getYaw(), client.player.getPitch())) {
                    chunkRenderManager.calculateChunksToRender(client, client.player.getPos(), client.player.getYaw(), client.player.getPitch());
                }
            }
            // Limpa o cache da GUI periodicamente ou em eventos específicos se necessário
            // Para simplicidade, não chamaremos clearCache aqui. Melhor em eventos de tela aberta/fechada.
        });

        // Registra um listener para desligar o AsyncResourceLoader quando o cliente do Minecraft desligar
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
            AsyncResourceLoader.shutdown(); // Desliga o pool de threads do carregador assíncrono
        });
    }
}
