package com.barium.mixin;

import com.barium.optimization.ChunkSavingOptimizer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin para ServerWorld para acionar o processamento da fila de salvamento de chunks.
 * Corrigido: Chamada para processSaveQueue() em vez de método inexistente.
 */
@Mixin(ServerWorld.class)
public class ServerWorldMixin {

    /**
     * Injeta no início do método save do ServerWorld.
     * O ideal seria injetar em um ponto onde o salvamento de chunks é gerenciado,
     * ou chamar processSaveQueue periodicamente através de um ServerTickEvent.
     * Injetar em 'save' pode ser muito frequente ou não cobrir todos os cenários.
     *
     * Target Method Signature (Yarn 1.21.5): Lnet/minecraft/server/world/ServerWorld;save(Lnet/minecraft/util/ProgressListener;ZZ)V
     * Ou talvez Lnet/minecraft/server/world/ServerWorld;tick(Ljava/util/function/BooleanSupplier;)V ?
     * Por enquanto, vamos usar um ponto seguro como o início do tick ou um evento.
     *
     * Alternativa: Usar ServerTickEvents.END_SERVER_TICK
     */
    @Inject(method = "tick(Ljava/util/function/BooleanSupplier;)V", at = @At("TAIL"))
    private void barium$onTickEnd(CallbackInfo ci) {
        // Processa a fila de salvamento de chunks no final de cada tick do servidor
        // Isso garante que os chunks sejam salvos periodicamente.
        ChunkSavingOptimizer.processSaveQueue();
    }

    /* 
    // Injeção original em save(), mantida como referência.
    @Inject(method = "save", at = @At("HEAD"))
    private void onSave(CallbackInfo ci) {
        // ServerWorld world = (ServerWorld)(Object)this; // Não necessário se chamado estaticamente
        
        // Processa o salvamento de chunks enfileirados
        // ChunkSavingOptimizer.processChunkSaving(world); // Método inexistente
        ChunkSavingOptimizer.processSaveQueue(); // Método correto
    }
    */
}

