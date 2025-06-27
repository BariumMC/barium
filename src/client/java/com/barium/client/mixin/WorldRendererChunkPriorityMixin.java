package com.barium.client.mixin;

import com.barium.client.BariumClient;
import com.barium.client.optimization.ChunkRenderPrioritizer;
import com.barium.client.optimization.ChunkUploadThrottler;
import com.barium.client.util.ChunkVisibilityManager; // Importa o culling de oclusão
import com.barium.config.BariumConfig; // Importa as configurações
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
    @Shadow @Final private MinecraftClient client; // Adicionado para acesso ao cliente

    /**
     * Injeta no método que prepara o terreno para renderização.
     * Este é o ponto central para atualizar TODOS os nossos managers de culling.
     */
    @Inject(method = "setupTerrain(Lnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/Frustum;ZZ)V", at = @At("HEAD"))
    private void barium$updateAllChunkManagers(Camera camera, Frustum frustum, boolean hasForcedFrustum, boolean spectator, CallbackInfo ci) {
        // 1. Atualiza o Frustum Culling (o que já existia)
        if (BariumConfig.C.ENABLE_FRUSTUM_CHUNK_CULLING) {
            BariumClient.getInstance().getChunkRenderManager().calculateChunksToRender(this.client, frustum);
        }
        
        // 2. Atualiza o Visibility Graph Culling (o que causa stutter)
        if (BariumConfig.C.ENABLE_VISIBILITY_GRAPH_CULLING) {
            ChunkVisibilityManager.getInstance().update(this.client);
        }
    }

    /**
     * Injeta ANTES de o jogo começar a processar a fila de chunks para rebuild/upload.
     * É o local ideal para preparar a priorização e o throttling.
     */
    @Inject(method = "updateChunks", at = @At("HEAD"))
    private void barium$beforeUpdateChunks(Camera camera, CallbackInfo ci) {
        // 1. Passa a posição da câmera para o ChunkBuilder para priorizar chunks próximos.
        this.chunkBuilder.setCameraPosition(camera.getPos());
        
        // 2. Reseta o contador do nosso limitador de uploads.
        ChunkUploadThrottler.resetCounter();
    }
}