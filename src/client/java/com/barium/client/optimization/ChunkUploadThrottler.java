package com.barium.client.optimization;

import com.barium.config.BariumConfig;
import java.util.Queue;

public class ChunkUploadThrottler {
    
    // Aumentei um pouco o orçamento para garantir fluidez no loading (3ms)
    private static final long BUDGET_NS = 3_000_000;
    private static long frameStartTime = 0;
    private static int uploadsThisFrame = 0;
    // Contadores para reduzir agressividade se estivermos ultrapassando o orçamento
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

        if (queue.isEmpty()) {
            return null;
        }

        long timeElapsed = System.nanoTime() - frameStartTime;

        // Calcula limite de uploads baseado na configuração e no modo de render (llvmpipe reduz agressivamente)
        int allowedUploads = BariumConfig.C.MAX_CHUNK_UPLOADS_PER_FRAME;
        if (BariumConfig.C.ENABLE_LLVMPIPE_MODE) {
            allowedUploads = Math.max(1, allowedUploads / 4);
        }

        // Aplica penalidade temporária se ultrapassamos o orçamento recentemente
        if (penaltyFrames > 0) {
            allowedUploads = Math.max(1, allowedUploads >> 1);
        }

        // Se já atingimos o número de uploads permitidos neste frame, bloqueia mais uploads.
        if (uploadsThisFrame >= allowedUploads) {
            return null;
        }

        // Lógica de "Fome Zero": se nenhum chunk foi enviado neste frame ainda, permita pelo menos um.
        if (timeElapsed >= BUDGET_NS && uploadsThisFrame > 0) {
            // marcamos que ultrapassamos o orçamento e aplicamos penalidade nas próximas frames
            recentOverBudgetCount++;
            penaltyFrames = Math.min(4, recentOverBudgetCount);
            return null;
        }

        // Se estamos bem abaixo do orçamento, podemos diminuir a penalidade com o tempo
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