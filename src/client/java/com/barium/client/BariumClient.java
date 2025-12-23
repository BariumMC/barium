package com.barium.client;

import com.barium.BariumMod;
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

    // CORREÇÃO: O ChunkRenderManager foi adicionado de volta
    private final ChunkRenderManager chunkRenderManager = ChunkRenderManager.getInstance();

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

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.world == null) {
                ChunkVisibilityManager.getInstance().clear();
                // CORREÇÃO: A chamada ao método clear() agora funcionará.
                this.chunkRenderManager.clear();
            }
        });

        BariumMod.LOGGER.info("Barium Client Initialized.");
    }

    private static int currentFps = 60; // default
    private static long lastFpsUpdate = 0;
    private static int frameCount = 0;

    public static int getCurrentFps() {
        return currentFps;
    }

    public static void setCurrentFps(int fps) {
        currentFps = fps;
    }

    public static void updateFps() {
        frameCount++;
        long now = System.currentTimeMillis();
        if (now - lastFpsUpdate >= 1000) { // update every second
            currentFps = frameCount;
            frameCount = 0;
            lastFpsUpdate = now;
        }
    }
}