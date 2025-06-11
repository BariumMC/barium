// --- Substitua o conteúdo em: src/client/java/com/barium/client/optimization/ChunkUploadThrottler.java ---
package com.barium.client.optimization;

import com.barium.config.BariumConfig;
import net.minecraft.client.render.chunk.ChunkBuilder;

import java.util.Queue;

public class ChunkUploadThrottler {

    // CORREÇÃO: Usamos a interface pública 'Runnable' em vez da classe privada 'UploadTask'
    public static int processUploadQueue(Queue<Runnable> uploadQueue) {
        if (!BariumConfig.ENABLE_CHUNK_UPDATE_THROTTLING) {
            int count = 0;
            while (!uploadQueue.isEmpty()) {
                Runnable task = uploadQueue.poll();
                if (task != null) {
                    task.run();
                    count++;
                }
            }
            return count;
        }

        int uploadsProcessed = 0;
        final int limit = BariumConfig.MAX_CHUNK_UPLOADS_PER_FRAME;

        while (uploadsProcessed < limit && !uploadQueue.isEmpty()) {
            Runnable task = uploadQueue.poll();
            if (task != null) {
                task.run();
                uploadsProcessed++;
            }
        }

        return uploadsProcessed;
    }
}