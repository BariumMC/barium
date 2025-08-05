package com.barium.client.mixin;

import com.barium.client.optimization.ChunkUploadThrottler;
import com.barium.client.optimization.VertexBufferUploader; // <-- NOVO IMPORT
import net.minecraft.client.render.BufferBuilder; // <-- NOVO IMPORT
import net.minecraft.client.render.VertexBuffer; // <-- NOVO IMPORT
import net.minecraft.client.render.chunk.ChunkBuilder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject; // <-- NOVO IMPORT
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable; // <-- NOVO IMPORT

import java.util.Queue;

@Mixin(ChunkBuilder.class)
public class ChunkBuilderMixin {

    // --- Injeção existente para limitar o upload de chunks ---
    @Redirect(
        method = "upload()V",
        at = @At(value = "INVOKE", target = "Ljava/util/Queue;poll()Ljava/lang/Object;")
    )
    private Object barium$throttleChunkUploads(Queue<?> uploadQueue) {
        return ChunkUploadThrottler.pollTask(uploadQueue);
    }

    // --- NOVA INJEÇÃO PARA OTIMIZAR O UPLOAD COM LWJGL ---
    /**
     * Intercepta o método 'upload' do ChunkBuilder ANTES que ele chame o método de upload do VertexBuffer.
     * Isso nos permite substituir completamente a lógica de upload pela nossa versão otimizada.
     *
     * @param vertexBuffer O buffer de destino na GPU.
     * @param buffer Os dados do chunk prontos para serem enviados.
     * @param cir A CallbackInfo que nos permite cancelar o método original.
     */
    @Inject(
        method = "upload(Lnet/minecraft/client/render/VertexBuffer;Lnet/minecraft/client/render/BufferBuilder$DrawParameters;)Lnet/minecraft/client/render/VertexBuffer$Draws;",
        at = @At("HEAD"),
        cancellable = true
    )
    private void barium$usePersistentMappedBuffersForUpload(VertexBuffer vertexBuffer, BufferBuilder.DrawParameters buffer, CallbackInfoReturnable<VertexBuffer.Draws> cir) {
        // Chama a nossa lógica de upload personalizada.
        VertexBufferUploader.upload(vertexBuffer, buffer);
        
        // CORREÇÃO CRÍTICA: O método original espera um valor de retorno.
        // Após o nosso upload, retornamos os mesmos dados de desenho que o método vanilla retornaria.
        // Isso satisfaz a assinatura do método e evita que o jogo quebre.
        cir.setReturnValue(vertexBuffer.createDraws(buffer.getVertexFormat(), buffer.getDraws()));

        // Ao chamar cir.setReturnValue(), nós efetivamente cancelamos a execução do resto do método original,
        // impedindo que o upload vanilla (mais lento) seja executado.
    }
}