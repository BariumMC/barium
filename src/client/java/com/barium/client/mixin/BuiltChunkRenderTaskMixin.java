package com.barium.client.mixin;

import com.barium.client.optimization.VertexBufferUploader;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.VertexBuffer;
import net.minecraft.client.render.chunk.ChunkBuilder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ChunkBuilder.BuiltChunk.RenderTask.class)
public class BuiltChunkRenderTaskMixin {

    /**
     * Redireciona a chamada de `VertexBuffer.upload()` para a nossa classe otimizada.
     * Em vez de o jogo chamar seu próprio método de upload, ele chamará o nosso.
     * Isso nos permite substituir a lógica de upload sem reescrever a classe inteira.
     *
     * @param instance O VertexBuffer no qual o upload seria chamado.
     * @param data Os dados de renderização do chunk a serem carregados.
     */
    @Redirect(
        method = "run(Ljava/util/function/Consumer;)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/render/VertexBuffer;upload(Lnet/minecraft/client/render/BufferBuilder$RenderBuffer;)V"
        )
    )
    private void barium$usePersistentMappedBuffers(VertexBuffer instance, BufferBuilder.RenderBuffer data) {
        // Chama nossa lógica de upload personalizada em vez da original.
        VertexBufferUploader.upload(instance, data);
    }
}