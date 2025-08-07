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
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.client.render.object.ObjectAllocator;
import net.minecraft.client.util.GpuBufferSlice;

import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin de Tomada de Controle Total (VERSÃO FINAL 1.21)
 * Substitui o método de renderização principal.
 */
@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin {

    @Shadow @Final private MinecraftClient client;
    @Shadow private @Nullable ClientWorld world;

    // As injeções da Parte 1 e 2 continuam como antes. Elas são estáveis.
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

    // --- PARTE 3: A NOVA E ROBUSTA TOMADA DE CONTROLE ---

    /**
     * **CORREÇÃO DEFINITIVA**: Injeta no início do método `render` principal do WorldRenderer.
     * Este método é o ponto de entrada chamado pelo `GameRenderer` a cada frame.
     * 
     * Ao cancelar este método, nós impedimos o Minecraft de desenhar CÉU, NUVENS, MUNDO,
     * PARTÍCULAS, etc., e tomamos o controle total.
     */
    @Inject(
        method = "render(Lnet/minecraft/client/render/object/ObjectAllocator;Lnet/minecraft/client/render/RenderTickCounter;ZLnet/minecraft/client/render/Camera;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/GameRenderer;Lnet/minecraft/client/render/LightmapTextureManager;Lorg/joml/Matrix4f;)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void barium$takeOverWorldRendering(
        ObjectAllocator objectAllocator, RenderTickCounter tickCounter, boolean renderBlockOutline,
        Camera camera, MatrixStack matrices, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager,
        Matrix4f projectionMatrix, CallbackInfo ci) {
        
        // --- ETAPA 1: Renderizar o que não é chunk (Opcional, mas recomendado) ---
        // Você pode chamar aqui os métodos originais para renderizar céu, nuvens, borda do mundo etc.
        // ou pode implementar sua própria lógica para eles.
        // Ex: this.renderSky(matrices, projectionMatrix, tickCounter.getTickDelta(), camera, renderWeather);

        // --- ETAPA 2: Chamar nosso renderizador de mundo ---
        // Esta é a chamada principal que substitui `renderBlockLayers`.
        BariumRenderManager.getInstance().renderWorld(matrices, camera);

        // --- ETAPA 3: Renderizar entidades e partículas (Opcional) ---
        // Ex: this.renderEntities(matrices, camera, ...);

        // Impede que o método de renderização original do Minecraft seja executado.
        ci.cancel();
    }

    // Shadow methods para que possamos chamar os métodos originais se quisermos (como renderSky)
    @Shadow
    public abstract void renderSky(MatrixStack matrices, Matrix4f projectionMatrix, float tickDelta, Camera camera, boolean bl);

    @Shadow
    public abstract void renderEntities(MatrixStack matrices, Camera camera, Frustum frustum, RenderTickCounter tickCounter);

}