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
     * Injeta no início do método `updateChunks` para garantir que a priorização
     * de chunks sempre use a posição mais recente da câmera.
     */
    @Inject(method = "updateChunks", at = @At("HEAD"))
    private void barium$updateCameraPositionForPriority(Camera camera, CallbackInfo ci) {
        ChunkRenderPrioritizer.updateCameraPosition(camera.getPos());
        this.chunkBuilder.setCameraPosition(camera.getPos());
    }

    /**
     * Injeta em `updateChunks` no exato momento ANTES da chamada ao método privado `scheduleRunTasks`.
     * Este é o local perfeito e mais robusto para resetar nosso contador de uploads por frame.
     */
    @Inject(
        method = "updateChunks",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/render/chunk/ChunkBuilder;scheduleRunTasks()V"
        )
    )
    private void barium$beforeScheduleTasks(Camera camera, CallbackInfo ci) {
        // Reseta o contador para o novo ciclo de uploads.
        ChunkUploadThrottler.resetCounter();
    }
}