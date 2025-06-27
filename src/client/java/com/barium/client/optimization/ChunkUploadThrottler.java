package com.barium.client.optimization;

import com.barium.config.BariumConfig;
import java.util.Queue;

public class ChunkUploadThrottler {
    private static int uploadsThisFrame = 0;

    public static void resetCounter() {
        uploadsThisFrame = 0;
    }

    public static Object pollTask(Queue<?> queue) {
        if (!BariumConfig.C.ENABLE_CHUNK_UPDATE_THROTTLING) {
            return queue.poll();
        }
        if (uploadsThisFrame >= BariumConfig.C.MAX_CHUNK_UPLOADS_PER_FRAME) {
            return null;
        }
        Object task = queue.poll();
        if (task != null) {
            uploadsThisFrame++;
        }
        return task;
    }
}