package com.barium.client.mixin;

import com.barium.client.optimization.ChunkRebuildOptimizer;
import com.barium.client.optimization.ChunkUploadThrottler;
import com.barium.config.BariumConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.chunk.ChunkBuilder;
import net.minecraft.client.render.chunk.ChunkRenderTaskScheduler; // Import necessário
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.ChunkSection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Queue;

@Mixin(ChunkBuilder.class)
public class ChunkBuilderMixin {

    // --- OTIMIZAÇÃO 1: Limitar Uploads de Chunks por Frame (Throttling de GPU) ---
    @Redirect(
        method = "upload()V",
        at = @At(value = "INVOKE", target = "Ljava/util/Queue;poll()Ljava/lang/Object;")
    )
    private Object barium$throttleChunkUploads(Queue<?> uploadQueue) {
        return ChunkUploadThrottler.pollTask(uploadQueue);
    }
    // --- OTIMIZAÇÃO 3: Limitar Reconstrução por Distância (LOD de CPU) ---

    private static final long NEAR_DISTANCE_SQ = 6 * 6;
    private static final long FAR_DISTANCE_SQ = 12 * 12;
    private static int distantRebuildCounter = 0;

    /**
     * Injeta no método que agenda a execução das tarefas (`scheduleRunTasks`).
     * Usando LocalCapture, obtemos acesso ao 'builtChunk' que está prestes a ser agendado.
     * Com base na sua distância, podemos cancelar o agendamento para limitar o rebuild de chunks distantes.
     */
    @Inject(
        method = "scheduleRunTasks()V", // Usando o método alvo confirmado.
        at = @At(
            value = "INVOKE",
            // Injetamos logo ANTES de a tarefa ser realmente enviada para o agendador.
            target = "Lnet/minecraft/client/render/chunk/ChunkRenderTaskScheduler;send(Lnet/minecraft/client/render/chunk/ChunkBuilder$BuiltChunk$Task;)V",
            shift = At.Shift.BEFORE
        ),
        cancellable = true,
        locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void barium$prioritizeAndThrottleRebuilds(
            // Parâmetro do método injetado
            CallbackInfo ci,
            // Variáveis locais que o Mixin captura para nós
            int i,
            ChunkBuilder.BuiltChunk builtChunk // Esta é a variável local que precisamos
    ) {
        if (!BariumConfig.C.ENABLE_DISTANCE_THROTTLING) {
            return;
        }

        // A lógica de otimização agora pode ser aplicada diretamente.
        BlockPos chunkOrigin = builtChunk.getOrigin();
        
        // Proteção contra NullPointerException se o jogador ainda não estiver no mundo.
        if (MinecraftClient.getInstance().player == null) {
            return;
        }
        ChunkPos cameraChunkPos = MinecraftClient.getInstance().player.getChunkPos();

        long dx = (chunkOrigin.getX() >> 4) - cameraChunkPos.x;
        long dz = (chunkOrigin.getZ() >> 4) - cameraChunkPos.z;
        long distSq = dx * dx + dz * dz;

        // Zona 1: Chunks próximos (0-6) -> Sempre processa.
        if (distSq <= NEAR_DISTANCE_SQ) {
            return; // Deixa o agendamento prosseguir.
        }

        // Zona 3: Chunks distantes (12+) -> Aplica limitação severa.
        if (distSq > FAR_DISTANCE_SQ) {
            distantRebuildCounter++;
            // Permite que apenas 1 em cada 30 tarefas de rebuild muito distante passe.
            if (distantRebuildCounter % 30 != 0) {
                ci.cancel(); // Cancela a chamada a `scheduler.send(task)`, pulando o agendamento.
            }
        }
        // Para chunks na Zona 2 (7-11), a tarefa também prossegue, e o jogo
        // já os trata com prioridade menor.
    }
}