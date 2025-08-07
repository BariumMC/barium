package com.barium.client.mixin;

import com.barium.client.render.BariumRenderManager;
import com.barium.client.optimization.ChunkUploadThrottler;
import com.barium.client.util.ChunkRenderManager;
import com.barium.client.util.ChunkVisibilityManager;
import com.barium.client.util.FloodFillVisibilityManager;
import com.barium.config.BariumConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin {

    @Shadow @Final private MinecraftClient client;
    @Shadow @Nullable private ClientWorld world;

    // --- Injeções de setup e schedule (ESTÁVEIS, SEM MUDANÇAS) ---

    @Inject(method = "setWorld", at = @At("HEAD"))
    private void barium$onSetWorld(@Nullable ClientWorld newWorld, CallbackInfo ci) {
        BariumRenderManager.getInstance().onWorldChange(newWorld);
    }

    @Inject(method = "setupTerrain(Lnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/Frustum;ZZ)V", at = @At("HEAD"))
    private void barium$updateAllChunkManagers(Camera camera, Frustum frustum, boolean hasForcedFrustum, boolean spectator, CallbackInfo ci) {
        if (this.world == null || this.client.player == null) return;
        if (BariumConfig.C.ENABLE_FRUSTUM_CHUNK_CULLING) ChunkRenderManager.getInstance().calculateChunksToRender(this.client, frustum);
        if (BariumConfig.C.ENABLE_FLOOD_FILL_CULLING) FloodFillVisibilityManager.getInstance().update(this.client);
        if (BariumConfig.C.ENABLE_VISIBILITY_GRAPH_CULLING) ChunkVisibilityManager.getInstance().update(this.client);
    }

    @Inject(method = "updateChunks(Lnet/minecraft/client/render/Camera;)V", at = @At("HEAD"))
    private void barium$beforeUpdateChunks(Camera camera, CallbackInfo ci) {
        ChunkUploadThrottler.resetCounter();
    }

    @Inject(method = "scheduleChunkRender(IIIZ)V", at = @At("HEAD"), cancellable = true)
    private void barium$takeOverRebuildScheduling(int x, int y, int z, boolean isPriority, CallbackInfo ci) {
        BariumRenderManager.getInstance().scheduleRebuild(x, y, z, isPriority);
        ci.cancel();
    }
    
    
    // --- TOMADA DE CONTROLE FINAL COM @REDIRECT ---
    
    /**
     * Esta é a injeção que substitui a renderização de chunks.
     * Ela intercepta CADA chamada para `renderLayer` dentro do loop do método `render` principal.
     * Em vez de deixar o Minecraft desenhar, nós chamamos nosso próprio manager.
     */
    @Redirect(
        method = "render(Lnet/minecraft/client/util/math/MatrixStack;FJZLnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/GameRenderer;Lnet/minecraft/client/render/LightmapTextureManager;Lorg/joml/Matrix4f;)V",
        at = @At(
            value = "INVOKE",
            // Este é o alvo exato. O `WorldRenderer` chamando seu próprio `renderLayer`
            target = "Lnet/minecraft/client/render/WorldRenderer;renderLayer(Lnet/minecraft/client/render/RenderLayer;Lnet/minecraft/client/util/math/MatrixStack;DDD)V"
        )
    )
    private void barium$redirectRenderLayer(
            // Os argumentos do @Redirect devem ser:
            // 1. A instância da classe que chama o método (`WorldRenderer`)
            // 2. Os argumentos do método que estamos interceptando (`renderLayer`)
            WorldRenderer instance, RenderLayer layer, MatrixStack matrices, 
            double cameraX, double cameraY, double cameraZ
    ) {
        // NÃO chamamos o método original (instance.renderLayer(...)).
        // Isso efetivamente desliga a renderização de chunks do Minecraft para este layer.

        // Em vez disso, chamamos nosso BariumRenderManager.
        BariumRenderManager.getInstance().renderLayer(matrices, layer, cameraX, cameraY, cameraZ);
    }
}