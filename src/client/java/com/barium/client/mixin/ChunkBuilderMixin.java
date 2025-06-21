// --- Substitua o conteúdo em: src/client/java/com/barium/client/mixin/ChunkBuilderMixin.java ---
package com.barium.client.mixin;

import com.barium.client.optimization.ChunkRebuildOptimizer;
import com.barium.client.optimization.ChunkUploadThrottler;
import net.minecraft.client.render.chunk.ChunkBuilder;
import net.minecraft.world.chunk.ChunkSection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Queue;

@Mixin(ChunkBuilder.class)
public class ChunkBuilderMixin {

    /**
     * Otimização de Upload: Limita quantos chunks são enviados para a GPU por frame.
     * Esta parte já estava correta.
     */
    @Redirect(
        method = "upload()V",
        at = @At(value = "INVOKE", target = "Ljava/util/Queue;poll()Ljava/lang/Object;")
    )
    private Object barium$throttleChunkUploads(Queue<Runnable> uploadQueue) {
        return ChunkUploadThrottler.pollTask(uploadQueue);
    }

    /**
     * Otimização de Rebuild: Pula seções de chunk vazias para economizar CPU.
     *
     * CORREÇÃO FINAL: Usando o seletor de método exato fornecido pelos mapeamentos.
     * A assinatura do método alvo é void e aceita BuiltChunk e ChunkRendererRegionBuilder.
     */
    @Redirect(
        method = "rebuild(Lnet/minecraft/client/render/chunk/ChunkBuilder$BuiltChunk;Lnet/minecraft/client/render/chunk/ChunkRendererRegionBuilder;)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/chunk/ChunkSection;isEmpty()Z")
    )
    private boolean barium$cullEmptyChunkSections(ChunkSection section) {
        // Delega a decisão para nossa classe de otimização, que respeita a config do usuário.
        return ChunkRebuildOptimizer.shouldSkipSection(section);
    }
}