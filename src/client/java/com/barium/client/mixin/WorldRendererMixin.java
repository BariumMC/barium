// Em: src/client/java/com/barium/client/mixin/WorldRendererMixin.java
package com.barium.client.mixin;

import com.barium.client.util.ChunkRenderManager;
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
     * Prepara os dados para o ChunkRenderMixin.
     * Injetamos no `setupTerrain` para capturar o Frustum e calcular nosso BitSet.
     * Esta é a abordagem mais estável.
     */
    @Inject(method = "setupTerrain(Lnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/Frustum;ZZ)V", at = @At("HEAD"))
    private void barium$updateChunkRenderManager(Camera camera, Frustum frustum, boolean hasForcedFrustum, boolean spectator, CallbackInfo ci) {
        if (BariumConfig.C.ENABLE_FRUSTUM_CHUNK_CULLING && this.client != null) {
            // A única função deste mixin é chamar o manager para calcular os chunks visíveis.
            ChunkRenderManager.getInstance().calculateChunksToRender(this.client, frustum);
        }
    }
}