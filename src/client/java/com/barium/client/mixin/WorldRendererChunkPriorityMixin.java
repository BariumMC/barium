package com.barium.client.mixin;

import com.barium.client.BariumClient;
import com.barium.client.util.ChunkRenderManager;
import com.barium.client.util.ChunkVisibilityManager;
import com.barium.client.util.FloodFillVisibilityManager; // Importa o novo manager
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
        if (client.world == null || client.player == null) {
            FloodFillVisibilityManager.getInstance().clear();
            ChunkVisibilityManager.getInstance().clear();
            return;
        }

        // Camada 1: Frustum Culling (a base)
        if (BariumConfig.C.ENABLE_FRUSTUM_CHUNK_CULLING) {
            BariumClient.getInstance().getChunkRenderManager().calculateChunksToRender(client, frustum);
        }

        // Camada 2: Otimização Estável por Flood-Fill (a nova otimização principal)
        if (BariumConfig.C.ENABLE_FLOOD_FILL_CULLING) {
            FloodFillVisibilityManager.getInstance().update(client);
        }

        // Camada 3: Otimização Experimental por Ray-Casting (antiga)
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