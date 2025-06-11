// --- Substitua o conteúdo em: src/client/java/com/barium/client/mixin/WorldRendererChunkPriorityMixin.java ---
package com.barium.client.mixin;

import com.barium.client.BariumClient; // Importe o BariumClient
import com.barium.client.optimization.ChunkRenderPrioritizer;
import com.barium.client.optimization.ChunkUploadThrottler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Frustum; // Importe o Frustum
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
     * Injeta no início do método `setupTerrain`, que é chamado a cada frame antes da
     * renderização do mundo. Este é o local perfeito para atualizar nosso ChunkRenderManager.
     *
     * @param camera A câmera do jogo.
     * @param frustum O frustum (campo de visão) calculado para o frame atual.
     * @param ci CallbackInfo.
     */
    @Inject(method = "setupTerrain(Lnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/Frustum;ZZ)V", at = @At("HEAD"))
    private void barium$updateChunkRenderManager(Camera camera, Frustum frustum, boolean hasForcedFrustum, boolean spectator, CallbackInfo ci) {
        // Pega a instância do nosso cliente e chama o método para calcular os chunks visíveis
        BariumClient.getInstance().getChunkRenderManager().calculateChunksToRender(MinecraftClient.getInstance(), frustum);
    }
    
    @Inject(method = "updateChunks", at = @At("HEAD"))
    private void barium$beforeUpdateChunks(Camera camera, CallbackInfo ci) {
        // Esta parte continua a mesma.
        ChunkRenderPrioritizer.updateCameraPosition(camera.getPos());
        this.chunkBuilder.setCameraPosition(camera.getPos());
        ChunkUploadThrottler.resetCounter();
    }
}