package com.barium.client.optimization;

import com.barium.config.BariumConfig;
import java.util.Queue;

public class ChunkUploadThrottler {
    
    // Aumentei um pouco o orçamento para garantir fluidez no loading (3ms)
    private static final long BUDGET_NS = 3_000_000; 
    private static long frameStartTime = 0;
    private static int uploadsThisFrame = 0;

    public static void resetCounter() {
        frameStartTime = System.nanoTime();
        uploadsThisFrame = 0;
    }

    public static Object pollTask(Queue<?> queue) {
        if (!BariumConfig.C.ENABLE_CHUNK_UPDATE_THROTTLING) {
            return queue.poll();
        }

        if (queue.isEmpty()) {
            return null;
        }

        long timeElapsed = System.nanoTime() - frameStartTime;
        
        // Lógica de "Fome Zero": Se nenhum chunk foi enviado neste frame ainda,
        // permita o envio mesmo que o tempo tenha passado. Isso evita travamentos eternos.
        if (timeElapsed >= BUDGET_NS && uploadsThisFrame > 0) {
            return null;
        }

        Object task = queue.poll();
        if (task != null) {
            uploadsThisFrame++;
        }
        return task;
    }
}