package com.barium.client.optimization;

import com.barium.config.BariumConfig;
import java.util.Queue;

public class ChunkUploadThrottler {

    private static int uploadsThisFrame = 0;

    /**
     * Reseta o contador. Deve ser chamado uma vez no início de cada frame.
     */
    public static void resetCounter() {
        uploadsThisFrame = 0;
    }

    /**
     * Tenta pegar uma tarefa da fila, respeitando o limite de uploads por frame.
     * Este método substitui a chamada direta para `queue.poll()`.
     * A assinatura genérica garante compatibilidade.
     *
     * @param queue A fila de tarefas de upload.
     * @return Uma tarefa se o limite não foi excedido, ou null caso contrário.
     */
    public static Object pollTask(Queue<?> queue) {
        // Se a otimização estiver desligada, apenas retorna o próximo item.
        if (!BariumConfig.C.ENABLE_CHUNK_UPDATE_THROTTLING) {
            return queue.poll();
        }

        // Se já atingimos o limite de uploads para este frame, não faz mais nada.
        if (uploadsThisFrame >= BariumConfig.C.MAX_CHUNK_UPLOADS_PER_FRAME) {
            return null;
        }

        // Pega a próxima tarefa da fila.
        Object task = queue.poll();

        // Se uma tarefa foi pega com sucesso, incrementa nosso contador.
        if (task != null) {
            uploadsThisFrame++;
        }

        return task;
    }
}