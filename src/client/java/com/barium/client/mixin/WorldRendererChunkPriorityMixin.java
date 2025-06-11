package com.barium.client.mixin;

import com.barium.client.optimization.ChunkRenderPrioritizer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.chunk.ChunkBuilder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public class WorldRendererChunkPriorityMixin {

    @Shadow private ChunkBuilder chunkBuilder;

    /**
     * Injeta no início do método `updateChunks`.
     * Antes que o WorldRenderer comece a decidir quais chunks precisam de atualização,
     * nós garantimos que nossa classe de priorização e o ChunkBuilder tenham a
     * posição mais recente da câmera.
     *
     * @param camera A câmera do jogo.
     * @param ci CallbackInfo.
     */
    @Inject(method = "updateChunks", at = @At("HEAD"))
    private void barium$updateCameraPositionForPriority(Camera camera, CallbackInfo ci) {
        // Atualiza nossa classe de otimização com a posição atual da câmera.
        ChunkRenderPrioritizer.updateCameraPosition(camera.getPos());

        // Também atualiza o ChunkBuilder do vanilla, o que aciona sua lógica de ordenação interna.
        // Fazer isso aqui garante que a ordenação ocorra com os dados mais frescos possíveis.
        this.chunkBuilder.setCameraPosition(camera.getPos());
    }
}