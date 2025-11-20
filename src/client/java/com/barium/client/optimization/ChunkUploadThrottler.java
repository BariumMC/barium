package com.barium.client.optimization;

import com.barium.config.BariumConfig;
import java.util.Queue;

public class ChunkUploadThrottler {
    
    // Orçamento de tempo padrão: 2.5 milissegundos (ajustável)
    private static final long BUDGET_NS = 2_500_000; 
    private static long frameStartTime = 0;

    public static void resetCounter() {
        frameStartTime = System.nanoTime();
    }

    public static Object pollTask(Queue<?> queue) {
        if (!BariumConfig.C.ENABLE_CHUNK_UPDATE_THROTTLING) {
            return queue.poll();
        }

        // Se a fila estiver vazia, retorna null rápido
        if (queue.isEmpty()) {
            return null;
        }

        // Verifica se já estouramos o orçamento de tempo deste frame
        long timeElapsed = System.nanoTime() - frameStartTime;
        
        // Se passamos do tempo limite, paramos de enviar chunks neste frame
        // para manter o FPS estável.
        if (timeElapsed >= BUDGET_NS) {
            return null;
        }

        // Se ainda temos tempo, processa a tarefa
        return queue.poll();
    }
}