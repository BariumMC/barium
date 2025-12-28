package com.barium.client.optimization;

import com.barium.config.BariumConfig;
import java.util.Queue;

public class ChunkUploadThrottler {
    
    private static final long BUDGET_NS = 3_000_000;
    private static long frameStartTime = 0;
    private static int uploadsThisFrame = 0;
    private static int recentOverBudgetCount = 0;
    private static int penaltyFrames = 0;

    public static void resetCounter() {
        frameStartTime = System.nanoTime();
        uploadsThisFrame = 0;
        if (penaltyFrames > 0) penaltyFrames = Math.max(0, penaltyFrames - 1);
    }

    public static Object pollTask(Queue<?> queue) {
        if (!BariumConfig.C.ENABLE_CHUNK_UPDATE_THROTTLING) {
            return queue.poll();
        }

        // --- ADIÇÃO PARA OTIMIZAR ROTAÇÃO ---
        // Se a câmera estiver girando rápido, bloqueamos uploads IMEDIATAMENTE.
        // Isso libera a CPU/GPU para focar apenas em desenhar os frames, eliminando o lag de virada.
        if (CameraRotationTracker.isRotatingFast()) {
            return null; 
        }
        // ------------------------------------

        if (queue.isEmpty()) {
            return null;
        }

        long timeElapsed = System.nanoTime() - frameStartTime;
        int allowedUploads = BariumConfig.C.MAX_CHUNK_UPLOADS_PER_FRAME;

        if (penaltyFrames > 0) {
            allowedUploads = Math.max(1, allowedUploads >> 1);
        }

        if (uploadsThisFrame >= allowedUploads) {
            return null;
        }

        if (timeElapsed >= BUDGET_NS && uploadsThisFrame > 0) {
            recentOverBudgetCount++;
            penaltyFrames = Math.min(4, recentOverBudgetCount);
            return null;
        }

        if (timeElapsed < (BUDGET_NS / 2) && recentOverBudgetCount > 0) {
            recentOverBudgetCount = Math.max(0, recentOverBudgetCount - 1);
        }

        Object task = queue.poll();
        if (task != null) {
            uploadsThisFrame++;
        }
        return task;
    }
}