package com.barium.client.mixin;

import com.barium.client.render.BariumRenderManager;
import com.barium.client.optimization.ChunkUploadThrottler;
import com.barium.client.util.ChunkRenderManager;
import com.barium.client.util.ChunkVisibilityManager;
import com.barium.client.util.FloodFillVisibilityManager;
import com.barium.config.BariumConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.chunk.ChunkBuilder;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin de Tomada de Controle Total para o WorldRenderer (VERSÃO CORRIGIDA E FUNCIONAL)
 */
@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin {

    @Shadow @Final private MinecraftClient client;
    @Shadow private @Nullable ClientWorld world;
    @Shadow private ChunkBuilder chunkBuilder;

    // --- PARTE 1: GANCHOS DE CICLO DE VIDA E ATUALIZAÇÃO ---

    @Inject(method = "setWorld", at = @At("HEAD"))
    private void barium$onSetWorld(@Nullable ClientWorld newWorld, CallbackInfo ci) {
        // Chamando o método que agora existe em BariumRenderManager
        BariumRenderManager.getInstance().onWorldChange(newWorld);
    }

    @Inject(method = "setupTerrain(Lnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/Frustum;ZZ)V", at = @At("HEAD"))
    private void barium$updateAllChunkManagers(Camera camera, Frustum frustum, boolean hasForcedFrustum, boolean spectator, CallbackInfo ci) {
        if (this.world == null || this.client.player == null) {
            return;
        }

        if (BariumConfig.C.ENABLE_FRUSTUM_CHUNK_CULLING) ChunkRenderManager.getInstance().calculateChunksToRender(this.client, frustum);
        if (BariumConfig.C.ENABLE_FLOOD_FILL_CULLING) FloodFillVisibilityManager.getInstance().update(this.client);
        if (BariumConfig.C.ENABLE_VISIBILITY_GRAPH_CULLING) ChunkVisibilityManager.getInstance().update(this.client);
    }

    @Inject(method = "updateChunks(Lnet/minecraft/client/render/Camera;)V", at = @At("HEAD"))
    private void barium$beforeUpdateChunks(Camera camera, CallbackInfo ci) {
        ChunkUploadThrottler.resetCounter();
    }


    // --- PARTE 2: TOMADA DE CONTROLE DO PROCESSO DE REBUILD ---

    @Inject(method = "scheduleRebuild", at = @At("HEAD"), cancellable = true)
    private void barium$takeOverRebuildScheduling(int x, int y, int z, boolean isPriority, CallbackInfo ci) {
        // Chamando o método que agora existe em BariumRenderManager
        BariumRenderManager.getInstance().scheduleRebuild(x, y, z, isPriority);
        ci.cancel();
    }


    // --- PARTE 3: TOMADA DE CONTROLE DO PROCESSO DE RENDERIZAÇÃO ---

    /**
     * **CORREÇÃO PRINCIPAL:** O alvo do @Inject agora é a assinatura correta do método `renderLayer`
     * para Minecraft 1.21.8, que usa um objeto Camera, e não três doubles.
     * Injetamos no início e cancelamos imediatamente, impedindo a execução do método original.
     */
    @Inject(
        method = "renderLayer(Lnet/minecraft/client/render/RenderLayer;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/Camera;)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void barium$takeoverAndRenderLayer(RenderLayer layer, MatrixStack matrices, Camera camera, CallbackInfo ci) {
        // Chamando o método CORRIGIDO em BariumRenderManager.
        // As coordenadas da câmera são obtidas de dentro do manager agora.
        BariumRenderManager.getInstance().renderLayer(matrices, layer, camera);
        
        // Cancela o resto do método vanilla.
        // NENHUMA renderização de chunk do Minecraft será executada para este layer.
        ci.cancel();
    }
}