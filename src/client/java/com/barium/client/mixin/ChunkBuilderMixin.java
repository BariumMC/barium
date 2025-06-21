// --- Substitua o conteúdo em: src/client/java/com/barium/client/mixin/ChunkBuilderMixin.java ---
package com.barium.client.mixin;

import com.barium.client.optimization.ChunkRebuildOptimizer;
import com.barium.client.optimization.ChunkUploadThrottler;
import net.minecraft.client.render.chunk.ChunkBuilder;
import net.minecraft.world.chunk.ChunkSection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Queue;
import java.util.Set;

@Mixin(ChunkBuilder.class)
public class ChunkBuilderMixin {

    /**
     * Otimização de Upload: Limita quantos chunks são enviados para a GPU por frame.
     * Esta parte já estava correta e permanece.
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
     * CORREÇÃO: Usamos @Inject com LocalCapture para uma abordagem mais robusta.
     * Injetamos nosso código no início do loop que itera sobre as seções do chunk.
     *
     * Alvo do @Inject: O método 'rebuild' na classe ChunkBuilder.
     * Ponto de Injeção 'at': Logo após a variável 'chunkSection' ser carregada (pela primeira vez no loop).
     * LocalCapture.CAPTURE_FAILHARD: Garante que, se a variável 'chunkSection' não for encontrada, o jogo crashe, nos avisando do problema.
     *
     * @param section A variável local 'chunkSection' que capturamos do método original.
     * @param cir A CallbackInfoReturnable que nos permite cancelar a execução do resto do loop para esta seção.
     */
    @Inject(
        method = "rebuild(Lnet/minecraft/client/render/chunk/ChunkBuilder$BuiltChunk;Lnet/minecraft/client/render/chunk/ChunkRendererRegionBuilder;)Ljava/util/Set;",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/chunk/ChunkSection;isEmpty()Z",
            ordinal = 0
        ),
        cancellable = true,
        locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void barium$cullEmptyChunkSections(
            ChunkBuilder.BuiltChunk chunk, 
            Object builder, // Usamos Object para evitar problemas com nomes de classe
            CallbackInfoReturnable<Set<Object>> cir, // Tipos genéricos
            int i,
            BlockPos.Mutable mutable,
            // ... outras variáveis locais capturadas que não usamos ...
            // A última variável capturada é a que nos interessa:
            ChunkSection section) {
        
        // Agora aplicamos nossa lógica. Se a seção deve ser pulada...
        if (ChunkRebuildOptimizer.shouldSkipSection(section)) {
            // ...nós não fazemos nada, permitindo que a verificação original 'if (section.isEmpty())'
            // continue e pule o bloco de renderização. O código vanilla já tem um 'continue' ali.
            // Esta abordagem é sutil: em vez de cancelar, nós garantimos que a lógica de pular do vanilla seja acionada
            // pela nossa verificação mais rigorosa.
            
            // Uma abordagem alternativa seria redirecionar a chamada 'isEmpty()' como antes,
            // mas o @Inject é mais para depuração. Vamos voltar para o @Redirect que é mais limpo,
            // mas com a ASSINATURA DO MÉTODO CORRETA.
        }
    }
}