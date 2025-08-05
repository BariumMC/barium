package com.barium.client.mixin;

import com.barium.client.util.ChunkRenderManager;
import com.barium.client.util.ChunkVisibilityManager;
import com.barium.client.util.FloodFillVisibilityManager;
import com.barium.client.optimization.ChunkUploadThrottler;
import com.barium.config.BariumConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.chunk.ChunkBuilder;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin {

    @Shadow @Final private MinecraftClient client;
    @Shadow private ChunkBuilder chunkBuilder;

    /**
     * Atualiza todos os managers de otimização de uma só vez.
     * Injeta em `setupTerrain` para ter acesso ao Frustum e um ponto de atualização confiável por frame.
     */
    @Inject(method = "setupTerrain(Lnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/Frustum;ZZ)V", at = @At("HEAD"))
    private void barium$updateAllChunkManagers(Camera camera, Frustum frustum, boolean hasForcedFrustum, boolean spectator, CallbackInfo ci) {
        if (client.world == null || client.player == null) {
            FloodFillVisibilityManager.getInstance().clear();
            ChunkVisibilityManager.getInstance().clear(); // Manager antigo (experimental)
            ChunkRenderManager.getInstance().clear();   // Manager de Frustum
            return;
        }

        // Camada 1: Frustum Culling (a base)
        if (BariumConfig.C.ENABLE_FRUSTUM_CHUNK_CULLING) {
            ChunkRenderManager.getInstance().calculateChunksToRender(this.client, frustum);
        }

        // Camada 2: Otimização Estável por Flood-Fill
        if (BariumConfig.C.ENABLE_FLOOD_FILL_CULLING) {
            FloodFillVisibilityManager.getInstance().update(this.client);
        }

        // Camada 3: Otimização Experimental por Ray-Casting (mantida, mas desativada por padrão)
        if (BariumConfig.C.ENABLE_VISIBILITY_GRAPH_CULLING) {
            ChunkVisibilityManager.getInstance().update(this.client);
        }
    }

    /**
     * Reseta contadores e prepara o chunk builder antes da fase de atualização de chunks.
     */
    @Inject(method = "updateChunks(Lnet/minecraft/client/render/Camera;)V", at = @At("HEAD"))
    private void barium$beforeUpdateChunks(Camera camera, CallbackInfo ci) {
        // Define a posição da câmera para priorizar a construção de chunks mais próximos
        if (this.chunkBuilder != null) {
            this.chunkBuilder.setCameraPosition(camera.getPos());
        }
        // Reseta o contador do limitador de uploads por frame
        ChunkUploadThrottler.resetCounter();
    }
}