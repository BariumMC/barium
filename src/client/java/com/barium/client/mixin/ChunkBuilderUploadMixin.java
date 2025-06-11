package com.barium.client.mixin;

import com.barium.client.optimization.ChunkUploadThrottler;
import net.minecraft.client.render.chunk.ChunkBuilder;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Queue;

@Mixin(ChunkBuilder.class)
public class ChunkBuilderUploadMixin {

    @Shadow @Final private Queue<ChunkBuilder.BuiltChunk.UploadTask> uploadQueue;

    /**
     * Injeta no final do método `runTasks`, que é responsável por processar os uploads.
     * Depois que o vanilla tentou (ou não) processar os uploads, nós garantimos que nossa
     * lógica de throttling seja aplicada. Esta é uma abordagem mais segura do que
     * redirecionar o loop interno.
     *
     * @param ci CallbackInfo
     */
    @Inject(method = "runTasks(Lnet/minecraft/client/render/Camera;)V", at = @At("HEAD"), cancellable = true)
    private void barium$takeControlOfUploads(CallbackInfo ci) {
        // Chamamos nossa classe de otimização para processar a fila.
        // Ela cuidará de aplicar o limite e executar as tarefas.
        ChunkUploadThrottler.processUploadQueue(this.uploadQueue);

        // Cancelamos o método original para impedir que o vanilla
        // execute qualquer outra lógica de upload sobre a nossa.
        ci.cancel();
    }
}