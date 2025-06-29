package com.barium.client.mixin;

import com.barium.client.BariumClient;
import com.barium.client.util.ChunkRenderManager;
import com.barium.client.util.ChunkVisibilityManager;
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
public abstract class WorldRendererChunkPriorityMixin {

    @Shadow @Final private MinecraftClient client;
    @Shadow private ChunkBuilder chunkBuilder;

    @Inject(method = "setupTerrain(Lnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/Frustum;ZZ)V", at = @At("HEAD"))
    private void barium$updateAllChunkManagers(Camera camera, Frustum frustum, boolean hasForcedFrustum, boolean spectator, CallbackInfo ci) {
        if (client.player == null) return;

        // Camada 1: Atualiza o Frustum Culling (a rede de segurança)
        if (BariumConfig.C.ENABLE_FRUSTUM_CHUNK_CULLING) {
            // CORREÇÃO: Acessando o manager através do getter que foi adicionado de volta.
            BariumClient.getInstance().getChunkRenderManager().calculateChunksToRender(client, frustum);
        }

        // Camada 2: Dispara a atualização do Visibility Graph (a otimização agressiva)
        if (BariumConfig.C.ENABLE_VISIBILITY_GRAPH_CULLING) {
            ChunkVisibilityManager.getInstance().update(client);
        }
    }

    @Inject(method = "updateChunks(Lnet/minecraft/client/render/Camera;)V", at = @At("HEAD"))
    private void barium$beforeUpdateChunks(Camera camera, CallbackInfo ci) {
        this.chunkBuilder.setCameraPosition(camera.getPos());
        ChunkUploadThrottler.resetCounter();
    }
}