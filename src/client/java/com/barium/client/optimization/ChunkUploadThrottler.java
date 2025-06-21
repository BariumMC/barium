// --- Edit this file: src/client/java/com/barium/client/optimization/ChunkUploadThrottler.java ---
package com.barium.client.optimization;

import com.barium.config.BariumConfig;
import java.util.Queue;

public class ChunkUploadThrottler {

    private static int uploadsThisFrame = 0;

    /**
     * Resets the counter. Should be called once at the beginning of each frame.
     */
    public static void resetCounter() {
        uploadsThisFrame = 0;
    }

    /**
     * Attempts to get a task from the queue, respecting the per-frame upload limit.
     * This method replaces the direct call to `queue.poll()`.
     *
     * CORRECTION: The method now works with a generic Queue<?> and returns an Object
     * to perfectly match the signature of the method it is redirecting.
     *
     * @param queue The queue of upload tasks.
     * @return A task object if the limit has not been exceeded, or null otherwise.
     */
    public static Object pollTask(Queue<?> queue) { // Changed to Queue<?>
        // If the optimization is turned off, just return the next item.
        if (!BariumConfig.C.ENABLE_CHUNK_UPDATE_THROTTLING) {
            return queue.poll();
        }

        // If we have already reached the upload limit for this frame, do nothing more.
        if (uploadsThisFrame >= BariumConfig.C.MAX_CHUNK_UPLOADS_PER_FRAME) {
            return null;
        }

        // Get the next task from the queue.
        Object task = queue.poll(); // Changed to Object

        // If a task was successfully retrieved, increment our counter.
        if (task != null) {
            uploadsThisFrame++;
        }

        return task;
    }
}