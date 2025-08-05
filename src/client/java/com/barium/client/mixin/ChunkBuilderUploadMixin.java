package com.barium.client.mixin;

import com.barium.client.optimization.VertexBufferUploader;
import net.minecraft.client.gl.VertexBuffer; // <-- CORREÇÃO: Import correto
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.chunk.ChunkBuilder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

// CORREÇÃO: O alvo do Mixin agora é a classe interna correta, 'ChunkBuilder.Upload'.
@Mixin(ChunkBuilder.Upload.class)
public class ChunkBuilderUploadMixin {

    /**
     * Redireciona a chamada de `VertexBuffer.upload()` para a nossa classe otimizada.
     * Em vez de o jogo chamar seu próprio método de upload, ele chamará o nosso.
     * Isso nos permite substituir a lógica de upload sem reescrever a classe inteira.
     *
     * @param instance O VertexBuffer no qual o upload seria chamado.
     * @param data     Os dados de renderização do chunk a serem carregados.
     */
    @Redirect(
        // CORREÇÃO: O método alvo é `run()`, que é executado para cada upload.
        method = "run()V",
        at = @At(
            value = "INVOKE",
            // CORREÇÃO: A assinatura de destino agora usa as classes corretas.
            target = "Lnet/minecraft/client/gl/VertexBuffer;upload(Lnet/minecraft/client/render/BufferBuilder$Buffer;)V"
        )
    )
    private void barium$usePersistentMappedBuffers(VertexBuffer instance, BufferBuilder.Buffer data) {
        // Chama nossa lógica de upload personalizada em vez da original.
        VertexBufferUploader.upload(instance, data);
    }
}