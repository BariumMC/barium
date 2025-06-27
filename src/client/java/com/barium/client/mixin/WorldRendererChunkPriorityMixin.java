package com.barium.client.mixin;

import com.barium.client.BariumClient;
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
    
    // CORREÇÃO: Removido o @Shadow problemático. Vamos obter o cliente de forma segura.

    /**
     * Injeta no método que prepara o terreno para renderização.
     * Este é o ponto central para atualizar TODOS os nossos managers de culling.
     */
    @Inject(method = "setupTerrain(Lnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/Frustum;ZZ)V", at = @At("HEAD"))
    private void barium$updateAllChunkManagers(Camera camera, Frustum frustum, boolean hasForcedFrustum, boolean spectator, CallbackInfo ci) {
        // Obtenção segura da instância do MinecraftClient
        MinecraftClient client = MinecraftClient.getInstance();

        // 1. Atualiza o Frustum Culling
        if (BariumConfig.C.ENABLE_FRUSTUM_CHUNK_CULLING) {
            BariumClient.getInstance().getChunkRenderManager().calculateChunksToRender(client, frustum);
        }
        
        // 2. Atualiza o Visibility Graph Culling
        if (BariumConfig.C.ENABLE_VISIBILITY_GRAPH_CULLING) {
            ChunkVisibilityManager.getInstance().update(client);
        }
    }

    /**
     * Injeta ANTES de o jogo começar a processar a fila de chunks para rebuild/upload.
     * É o local ideal para preparar a priorização e o throttling.
     */
    @Inject(method = "updateChunks(Lnet/minecraft/client/render/Camera;)V", at = @At("HEAD"))
    private void barium$beforeUpdateChunks(Camera camera, CallbackInfo ci) {
        // CORREÇÃO: A assinatura do método updateChunks foi corrigida para incluir o parâmetro Camera.
        
        // 1. Passa a posição da câmera para o ChunkBuilder para priorizar chunks próximos.
        this.chunkBuilder.setCameraPosition(camera.getPos());
        
        // 2. Reseta o contador do nosso limitador de uploads.
        // ChunkUploadThrottler.resetCounter(); // Esta linha pode ser re-adicionada se você tiver o Throttler
    }
}