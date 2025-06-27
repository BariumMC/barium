package com.barium.client.mixin;

import com.barium.client.BariumClient;
import com.barium.client.optimization.ChunkRenderPrioritizer;
import com.barium.client.optimization.ChunkUploadThrottler; // Importe o Throttler
import com.barium.client.util.ChunkVisibilityManager;
import com.barium.config.BariumConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Frustum;
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
     * Injeta no método que prepara o terreno para renderização.
     * Perfeito para atualizar nossa lista de chunks visíveis (Frustum Culling).
     */
    @Inject(method = "setupTerrain(Lnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/Frustum;ZZ)V", at = @At("HEAD"))
    private void barium$updateChunkRenderManager(Camera camera, Frustum frustum, boolean hasForcedFrustum, boolean spectator, CallbackInfo ci) {
        // Obtenção segura da instância do MinecraftClient
        MinecraftClient client = MinecraftClient.getInstance();

        // Atualiza o Frustum Culling
        if (BariumConfig.C.ENABLE_FRUSTUM_CHUNK_CULLING) {
            BariumClient.getInstance().getChunkRenderManager().calculateChunksToRender(client, frustum);
        }

        // Dispara a atualização do Visibility Graph em segundo plano
        if (BariumConfig.C.ENABLE_VISIBILITY_GRAPH_CULLING) {
            ChunkVisibilityManager.getInstance().update(client);
        }
    }

    /**
     * Injeta ANTES de o jogo começar a processar a fila de chunks para rebuild/upload.
     * É o local ideal para preparar nossas otimizações.
     */
    @Inject(method = "updateChunks(Lnet/minecraft/client/render/Camera;)V", at = @At("HEAD"))
    private void barium$beforeUpdateChunks(Camera camera, CallbackInfo ci) {
        // 1. Atualiza a posição da câmera para a lógica de priorização por distância.
        ChunkRenderPrioritizer.updateCameraPosition(camera.getPos());
        
        // 2. Passa a posição da câmera para o ChunkBuilder, que usa para ordenar as tarefas de reconstrução.
        this.chunkBuilder.setCameraPosition(camera.getPos());
        
        // 3. Reseta o contador do nosso limitador de uploads.
        ChunkUploadThrottler.resetCounter();
    }
}