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

    // --- OTIMIZAÇÃO 1: Limitar Uploads de Chunks por Frame (Throttling de GPU) ---
    /**
     * Intercepta a fila de uploads para a GPU e permite que apenas um número
     * limitado de chunks passe por frame, evitando picos de lag (stutter).
     */
    @Redirect(
        method = "upload()V",
        at = @At(value = "INVOKE", target = "Ljava/util/Queue;poll()Ljava/lang/Object;")
    )
    private Object barium$throttleChunkUploads(Queue<?> uploadQueue) {
        // A lógica é encapsulada no Throttler para manter o mixin limpo.
        return ChunkUploadThrottler.pollTask(uploadQueue);
    }
    // --- NOVA OTIMIZAÇÃO 3: Limitar Reconstrução por Distância (LOD de CPU) ---
    
    // Distâncias ao quadrado, em chunks, para definir as zonas de otimização.
    private static final long NEAR_DISTANCE_SQ = 6 * 6; // 0-6 chunks: alta prioridade
    private static final long FAR_DISTANCE_SQ = 12 * 12; // 12+ chunks: baixa prioridade
    
    // Contador para limitar rebuilds muito distantes.
    private static int distantRebuildCounter = 0;

    /**
     * Intercepta o envio de tarefas de reconstrução para aplicar nossa lógica de LOD.
     * Chunks distantes terão sua reconstrução severamente limitada, simulando o
     * efeito de uma distância de renderização menor, mas mantendo a visão do horizonte.
     */
    @Inject(method = "send(Lnet/minecraft/client/render/chunk/ChunkBuilder$BuiltChunk$Task;)V", at = @At("HEAD"), cancellable = true)
    private void barium$prioritizeAndThrottleRebuilds(ChunkBuilder.BuiltChunk.Task task, CallbackInfo ci) {
        if (!BariumConfig.C.ENABLE_DISTANCE_THROTTLING) {
            return;
        }

        // Obtém a posição do chunk da tarefa e a posição do jogador.
        BlockPos chunkOrigin = task.getChunk().getOrigin();
        ChunkPos cameraChunkPos = MinecraftClient.getInstance().player.getChunkPos();

        long dx = (chunkOrigin.getX() >> 4) - cameraChunkPos.x;
        long dz = (chunkOrigin.getZ() >> 4) - cameraChunkPos.z;
        long distSq = dx * dx + dz * dz;

        // Zona 1: Chunks próximos (0-6) -> Sempre processa com alta prioridade.
        if (distSq <= NEAR_DISTANCE_SQ) {
            return; // Deixa a tarefa prosseguir.
        }

        // Zona 2: Chunks distantes (12+) -> Aplica limitação severa.
        if (distSq > FAR_DISTANCE_SQ) {
            distantRebuildCounter++;
            // Apenas permite que 1 em cada 30 tarefas de rebuild muito distante passe.
            if (distantRebuildCounter % 30 != 0) {
                ci.cancel(); // Cancela o envio da tarefa de reconstrução.
            }
        }
        // Para chunks na zona média (7-11), a tarefa prossegue, mas o jogo já
        // os coloca com prioridade menor, o que é o comportamento desejado.
    }
}