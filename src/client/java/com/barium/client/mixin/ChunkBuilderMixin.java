package com.barium.client.mixin;

import com.barium.client.optimization.ChunkUploadThrottler;
import net.minecraft.client.render.chunk.ChunkBuilder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Queue;

/**
 * Este Mixin agora contém apenas as otimizações que se aplicam
 * diretamente à classe principal ChunkBuilder.
 */
@Mixin(ChunkBuilder.class)
public class ChunkBuilderMixin {

    // --- OTIMIZAÇÃO: Limitar Uploads de Chunks (Throttling de GPU) ---
    @Redirect(
        method = "upload()V",
        at = @At(value = "INVOKE", target = "Ljava/util/Queue;poll()Ljava/lang/Object;")
    )
    private Object barium$throttleChunkUploads(Queue<?> uploadQueue) {
        return ChunkUploadThrottler.pollTask(uploadQueue);
    }

    // A lógica de rebuild e de distância foram removidas daqui,
    // pois a de rebuild tem seu próprio Mixin e a de distância era instável.
}