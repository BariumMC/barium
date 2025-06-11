// --- Substitua o conteúdo em: src/client/java/com/barium/client/mixin/WorldRendererChunkPriorityMixin.java ---
package com.barium.client.mixin;

import com.barium.client.optimization.ChunkRenderPrioritizer;
import com.barium.client.optimization.ChunkUploadThrottler;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.chunk.ChunkBuilder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public class WorldRendererChunkPriorityMixin {

    @Shadow private ChunkBuilder chunkBuilder;

    /**
     * Injeta no início do método `updateChunks`. Este é o local mais robusto e
     * ideal para fazer todos os nossos preparativos de atualização de chunks,
     * incluindo a priorização e o reset do contador de throttling.
     *
     * @param camera A câmera do jogo, fornecida pelo método original.
     * @param ci CallbackInfo.
     */
    @Inject(method = "updateChunks", at = @At("HEAD"))
    private void barium$beforeUpdateChunks(Camera camera, CallbackInfo ci) {
        // 1. Atualiza a posição da câmera para a lógica de priorização.
        ChunkRenderPrioritizer.updateCameraPosition(camera.getPos());
        this.chunkBuilder.setCameraPosition(camera.getPos());

        // 2. Reseta o contador de uploads para o novo ciclo de trabalho.
        // Como isso é feito no início, garantimos que o limite de uploads
        // estará correto quando a thread do ChunkBuilder for executada.
        ChunkUploadThrottler.resetCounter();
    }
}