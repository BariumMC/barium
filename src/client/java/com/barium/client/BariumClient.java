package com.barium.client;

import com.barium.BariumMod;
// Import para o nosso Render Manager
import com.barium.client.render.BariumRenderManager; 
import com.barium.client.util.ChunkRenderManager;
import com.barium.client.util.ChunkVisibilityManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

@Environment(EnvType.CLIENT)
public class BariumClient implements ClientModInitializer {

    private static BariumClient instance;

    // Seu código original
    private final ChunkRenderManager chunkRenderManager = ChunkRenderManager.getInstance();

    // Seu RENDER_THREAD_POOL original
    public static final ExecutorService RENDER_THREAD_POOL = Executors.newSingleThreadExecutor(new ThreadFactory() {
        private final AtomicInteger threadId = new AtomicInteger(0);
        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r, "Barium Render Thread #" + threadId.incrementAndGet());
            t.setDaemon(true);
            t.setPriority(Thread.MIN_PRIORITY);
            return t;
        }
    });

    @Override
    public void onInitializeClient() {
        instance = this;
        BariumMod.LOGGER.info("Initializing Barium Client...");
        
        // ====================================================================
        // A CORREÇÃO ADICIONADA:
        // Inicializa nosso sistema de renderização customizado aqui.
        // Isso garante que o 'mesherExecutor' e outros componentes sejam
        // criados antes que o jogo tente usá-los, resolvendo o NullPointerException.
        BariumRenderManager.getInstance().init();
        // ====================================================================

        // Seu código original para o Tick Event
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.world == null) {
                ChunkVisibilityManager.getInstance().clear();
                this.chunkRenderManager.clear();
            }
        });

        BariumMod.LOGGER.info("Barium Client Initialized.");
    }

    public static BariumClient getInstance() {
        return instance;
    }
    
    public ChunkRenderManager getChunkRenderManager() {
        return chunkRenderManager;
    }
}
