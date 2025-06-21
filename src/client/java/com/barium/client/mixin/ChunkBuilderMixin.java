// --- Adicione este novo arquivo em: src/client/java/com/barium/client/mixin/ChunkBuilderMixin.java ---
package com.barium.client.mixin;

import com.barium.client.optimization.ChunkUploadThrottler;
import net.minecraft.client.render.chunk.ChunkBuilder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Queue;

@Mixin(ChunkBuilder.class)
public class ChunkBuilderMixin {

    /**
     * Redireciona a chamada a `uploadQueue.poll()` dentro do método `upload()`.
     * Em vez de pegar a próxima tarefa da fila diretamente, a chamada passa pelo nosso
     * Throttler, que só retornará uma tarefa se o limite de uploads por frame não tiver sido atingido.
     *
     * Esta é a forma mais limpa e compatível de implementar o throttling.
     */
    @Redirect(
        method = "upload()V",
        at = @At(value = "INVOKE", target = "Ljava/util/Queue;poll()Ljava/lang/Object;")
    )
    private Object barium$throttleChunkUploads(Queue<Runnable> uploadQueue) {
        // A nossa lógica de limitação é chamada em vez do queue.poll() original.
        return ChunkUploadThrottler.pollTask(uploadQueue);
    }
}