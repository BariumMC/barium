// --- Verifique e substitua o conteúdo em: src/client/java/com/barium/client/optimization/ChunkUploadThrottler.java ---
package com.barium.client.optimization;

import com.barium.config.BariumConfig;
import java.util.Queue;

public class ChunkUploadThrottler {

    private static int uploadsThisFrame = 0;

    /**
     * CORREÇÃO: Adiciona este método se ele estiver faltando.
     * Reseta o contador de uploads. Deve ser chamado uma vez no início de cada ciclo de agendamento.
     */
    public static void resetCounter() {
        uploadsThisFrame = 0;
    }

    /**
     * Tenta pegar uma tarefa da fila, respeitando o limite de uploads por frame.
     * Este método substitui a chamada direta a `queue.poll()`.
     */
    public static Runnable pollTask(Queue<Runnable> queue) {
        if (!BariumConfig.ENABLE_CHUNK_UPDATE_THROTTLING) {
            return queue.poll();
        }

        if (uploadsThisFrame >= BariumConfig.MAX_CHUNK_UPLOADS_PER_FRAME) {
            return null;
        }

        Runnable task = queue.poll();

        if (task != null) {
            uploadsThisFrame++;
        }

        return task;
    }
}