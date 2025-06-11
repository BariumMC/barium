// --- Nenhuma mudança necessária aqui, apenas verifique se está igual ---
// src/client/java/com/barium/client/mixin/WorldRendererChunkPriorityMixin.java
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

    @Inject(method = "updateChunks", at = @At("HEAD"))
    private void barium$updateCameraPositionForPriority(Camera camera, CallbackInfo ci) {
        ChunkRenderPrioritizer.updateCameraPosition(camera.getPos());
        this.chunkBuilder.setCameraPosition(camera.getPos());
    }

    @Inject(
        method = "updateChunks",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/render/chunk/ChunkBuilder;scheduleRunTasks()V"
        )
    )
    private void barium$beforeScheduleTasks(Camera camera, CallbackInfo ci) {
        ChunkUploadThrottler.resetCounter();
    }
}