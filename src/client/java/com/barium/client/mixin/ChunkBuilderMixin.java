package com.barium.client.mixin;

import com.barium.client.optimization.ChunkRebuildOptimizer;
import com.barium.client.optimization.ChunkUploadThrottler;
import com.barium.config.BariumConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.chunk.ChunkBuilder;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.ChunkSection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Queue;

@Mixin(ChunkBuilder.class)
public class ChunkBuilderMixin {

    // ... (As outras duas otimizações permanecem as mesmas) ...
    @Redirect(
        method = "upload()V",
        at = @At(value = "INVOKE", target = "Ljava/util/Queue;poll()Ljava/lang/Object;")
    )
    private Object barium$throttleChunkUploads(Queue<?> uploadQueue) {
        return ChunkUploadThrottler.pollTask(uploadQueue);
    }

    @Redirect(
        method = "rebuild(Lnet/minecraft/client/render/chunk/ChunkBuilder$BuiltChunk;Lnet/minecraft/client/render/chunk/ChunkRendererRegionBuilder;)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/chunk/ChunkSection;isEmpty()Z")
    )
    private boolean barium$cullEmptyChunkSections(ChunkSection section) {
        return ChunkRebuildOptimizer.shouldSkipSection(section);
    }
    
    // --- Otimização de Distância (CORRIGIDA) ---
    private static final long NEAR_DISTANCE_SQ = 6 * 6;
    private static final long FAR_DISTANCE_SQ = 12 * 12;
    private static int distantRebuildCounter = 0;

    @Inject(method = "send(Lnet/minecraft/client/render/chunk/ChunkBuilder$BuiltChunk$Task;)V", at = @At("HEAD"), cancellable = true)
    private void barium$prioritizeAndThrottleRebuilds(ChunkBuilder.BuiltChunk.Task task, CallbackInfo ci) {
        if (!BariumConfig.C.ENABLE_DISTANCE_THROTTLING) {
            return;
        }

        // CORREÇÃO: Usamos o nosso Accessor para obter o BuiltChunk.
        // Primeiro, fazemos um cast da tarefa para a nossa interface Accessor.
        BuiltChunkTaskAccessor accessor = (BuiltChunkTaskAccessor) task;
        ChunkBuilder.BuiltChunk builtChunk = accessor.getChunk();

        // Se por algum motivo o chunk for nulo, não fazemos nada.
        if (builtChunk == null) {
            return;
        }

        BlockPos chunkOrigin = builtChunk.getOrigin();
        ChunkPos cameraChunkPos = MinecraftClient.getInstance().player.getChunkPos();

        long dx = (chunkOrigin.getX() >> 4) - cameraChunkPos.x;
        long dz = (chunkOrigin.getZ() >> 4) - cameraChunkPos.z;
        long distSq = dx * dx + dz * dz;

        if (distSq <= NEAR_DISTANCE_SQ) {
            return;
        }

        if (distSq > FAR_DISTANCE_SQ) {
            distantRebuildCounter++;
            if (distantRebuildCounter % 30 != 0) {
                ci.cancel();
            }
        }
    }
}