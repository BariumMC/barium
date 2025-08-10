package com.barium.client;

import com.barium.BariumMod;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

@Environment(EnvType.CLIENT)
public class BariumClient implements ClientModInitializer {

    // RESTAURADO: Esta thread é usada pelos seus managers de visibilidade.
    public static final ExecutorService RENDER_THREAD_POOL = Executors.newSingleThreadExecutor(new ThreadFactory() {
        private final AtomicInteger threadId = new AtomicInteger(0);
        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r, "Barium Visibility Thread #" + threadId.incrementAndGet());
            t.setDaemon(true);
            return t;
        }
    });

    @Override
    public void onInitializeClient() {
        BariumMod.LOGGER.info("Barium Client está inicializando.");
        // A inicialização do Render Manager é feita sob demanda (lazy) e está correta.
    }
}