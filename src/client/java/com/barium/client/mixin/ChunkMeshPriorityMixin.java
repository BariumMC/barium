package com.barium.client.mixin;

import com.barium.client.optimization.ChunkMeshPriorityOptimizer;
import net.minecraft.client.render.chunk.ChunkBuilder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.CompletableFuture;

/**
 * Mixin para integrar o otimizador de prioridade de geração de mesh de chunks
 */
@Mixin(ChunkBuilder.class)
public class ChunkMeshPriorityMixin {
    
    /**
     * Injeta no método de agendamento de chunks para renderização
     */
    @Inject(
        method = "scheduleRebuild",
        at = @At("HEAD"),
        cancellable = true
    )
    private void onScheduleRebuild(int x, int y, int z, boolean important, CallbackInfo ci) {
        ChunkBuilder builder = (ChunkBuilder)(Object)this;
        ChunkBuilder.BuiltChunk chunk = builder.getRenderedChunk(x, y, z);
        
        if (chunk != null) {
            // Agenda o chunk com prioridade adaptativa
            boolean scheduled = ChunkMeshPriorityOptimizer.scheduleChunkRender(chunk);
            
            if (scheduled) {
                ci.cancel(); // Cancela o método original, pois já agendamos o chunk
            }
        }
    }
    
    /**
     * Injeta no método de obtenção do próximo chunk a processar
     */
    @Inject(
        method = "getNextChunkToRender",
        at = @At("HEAD"),
        cancellable = true
    )
    private void onGetNextChunkToRender(CallbackInfoReturnable<ChunkBuilder.BuiltChunk> cir) {
        // Obtém o próximo chunk com base na prioridade adaptativa
        ChunkBuilder.BuiltChunk nextChunk = ChunkMeshPriorityOptimizer.getNextChunkToProcess();
        
        if (nextChunk != null) {
            cir.setReturnValue(nextChunk);
        }
    }
    
    /**
     * Modifica o número de threads de renderização
     */
    @ModifyVariable(
        method = "setThreadCount",
        at = @At("HEAD"),
        ordinal = 0
    )
    private int adjustThreadCount(int threadCount) {
        // Ajusta o número de threads com base na carga atual
        return ChunkMeshPriorityOptimizer.adjustRenderThreadCount(threadCount);
    }
}
