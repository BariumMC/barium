// --- TEMPORARY TEST - Replace the content of: src/client/java/com/barium/client/mixin/ChunkBuilderMixin.java ---
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
     * Upload Throttling optimization. This part is stable and remains unchanged.
     */
    @Redirect(
        method = "upload()V",
        at = @At(value = "INVOKE", target = "Ljava/util/Queue;poll()Ljava/lang/Object;")
    )
    private Object barium$throttleChunkUploads(Queue<Runnable> uploadQueue) {
        return ChunkUploadThrottler.pollTask(uploadQueue);
    }

    /*
     *  THE REBUILD OPTIMIZATION IS TEMPORARILY DISABLED TO ISOLATE THE CRASH
     *
    @Redirect(
        method = "rebuild(Lnet/minecraft/client/render/chunk/ChunkBuilder$BuiltChunk;Lnet/minecraft/client/render/chunk/ChunkRendererRegionBuilder;)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/chunk/ChunkSection;isEmpty()Z")
    )
    private boolean barium$cullEmptyChunkSections(ChunkSection section) {
        return ChunkRebuildOptimizer.shouldSkipSection(section);
    }
    */
}