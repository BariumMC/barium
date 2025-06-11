package com.barium.client.optimization;

import com.barium.config.BariumConfig;
import net.minecraft.client.render.chunk.ChunkBuilder;

import java.util.Queue;

/**
 * Controla o "afogamento" (throttling) do upload de dados de chunk para a GPU.
 * Em vez de enviar todos os chunks reconstruídos de uma vez em um único frame,
 * esta classe limita o número de uploads, espalhando a carga ao longo de vários
 * frames e evitando picos de lag (stutters).
 */
public class ChunkUploadThrottler {

    /**
     * Processa a fila de tarefas de upload de chunk, respeitando um limite configurável.
     *
     * @param uploadQueue A fila de tarefas de upload do ChunkBuilder.
     * @return O número de uploads que foram realmente processados.
     */
    public static int processUploadQueue(Queue<ChunkBuilder.BuiltChunk.UploadTask> uploadQueue) {
        if (!BariumConfig.ENABLE_CHUNK_UPDATE_THROTTLING) {
            // Se a otimização estiver desativada, processamos tudo (ou deixamos o vanilla fazer)
            int count = 0;
            while (!uploadQueue.isEmpty()) {
                ChunkBuilder.BuiltChunk.UploadTask task = uploadQueue.poll();
                if (task != null) {
                    task.run();
                    count++;
                }
            }
            return count;
        }

        int uploadsProcessed = 0;
        final int limit = BariumConfig.MAX_CHUNK_UPLOADS_PER_FRAME;

        // Processa a fila até o limite ou até ela ficar vazia
        while (uploadsProcessed < limit && !uploadQueue.isEmpty()) {
            ChunkBuilder.BuiltChunk.UploadTask task = uploadQueue.poll();
            if (task != null) {
                task.run(); // Executa a tarefa de upload
                uploadsProcessed++;
            }
        }

        return uploadsProcessed;
    }
}