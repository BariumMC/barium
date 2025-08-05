package com.barium.client.mixin;

import com.barium.client.optimization.ChunkUploadThrottler;
import net.minecraft.client.render.chunk.ChunkBuilder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Queue;

@Mixin(ChunkBuilder.class)
public class ChunkBuilderMixin {

    @Redirect(
        method = "upload()V",
        at = @At(value = "INVOKE", target = "Ljava/util/Queue;poll()Ljava/lang/Object;")
    )
    private Object barium$throttleChunkUploads(Queue<?> uploadQueue) {
        return ChunkUploadThrottler.pollTask(uploadQueue);
    }

    // A injeção @Inject para 'barium$usePersistentMappedBuffersForUpload' foi completamente removida.
}