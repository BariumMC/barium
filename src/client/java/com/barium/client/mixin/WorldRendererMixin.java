package com.barium.client.mixin;

import com.barium.client.util.ChunkRenderManager;
import com.barium.client.util.ChunkVisibilityManager; // Importa o novo manager
import com.barium.config.BariumConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.WorldRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin {

    @Shadow @Final private MinecraftClient client;

    /**
     * Prepara os dados para as otimizações de chunk.
     * Injetamos no `setupTerrain` para ter acesso ao Frustum e um ponto de atualização confiável por frame.
     */
    @Inject(method = "setupTerrain(Lnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/Frustum;ZZ)V", at = @At("HEAD"))
    private void barium$updateChunkOptimizationManagers(Camera camera, Frustum frustum, boolean hasForcedFrustum, boolean spectator, CallbackInfo ci) {
        // Atualiza o manager de frustum culling, se habilitado.
        if (BariumConfig.C.ENABLE_FRUSTUM_CHUNK_CULLING && this.client != null) {
            ChunkRenderManager.getInstance().calculateChunksToRender(this.client, frustum);
        }

        // Atualiza o novo manager de visibilidade por ray-casting, se habilitado.
        if (BariumConfig.C.ENABLE_VISIBILITY_GRAPH_CULLING && this.client != null) {
            ChunkVisibilityManager.getInstance().update(this.client);
        }
    }
}