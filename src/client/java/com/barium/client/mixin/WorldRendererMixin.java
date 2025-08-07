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
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4fc;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin {

    @Shadow @Final private MinecraftClient client;
    @Shadow private @Nullable ClientWorld world;

    // --- Injeções estáveis de setup e schedule (sem mudanças) ---

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

    // --- TOMADA DE CONTROLE FINAL E CORRETA ---

    /**
     * **CORREÇÃO FINAL:** Injeta diretamente em `renderBlockLayers`. Este é o método que,
     * segundo a sua lista, é responsável por desenhar as camadas de blocos.
     * 
     * Cancelamos o método original e executamos nossa própria lógica em seu lugar.
     */
    @Inject(
        // O descritor completo do método para garantir que o alvo seja encontrado.
        // `Lnet/minecraft/client/render/WorldRenderer$SectionRenderState;` é o tipo de retorno.
        method = "renderBlockLayers(Lorg/joml/Matrix4fc;DDD)Lnet/minecraft/client/render/WorldRenderer$SectionRenderState;",
        at = @At("HEAD"),
        cancellable = true
    )
    private void barium$takeOverBlockLayerRendering(
            Matrix4fc matrix, double cameraX, double cameraY, double cameraZ,
            // Usamos CallbackInfoReturnable porque o método original retorna um valor.
            CallbackInfoReturnable<Object> cir 
    ) {
        MatrixStack matrices = new MatrixStack();
        matrices.peek().getPositionMatrix().mul(matrix);

        // **CORREÇÃO DE ACESSO:** Como os campos são privados, usamos os getters públicos.
        List<RenderLayer> blockLayers = new ArrayList<>();
        blockLayers.add(RenderLayer.getSolid());
        blockLayers.add(RenderLayer.getCutoutMipped());
        blockLayers.add(RenderLayer.getCutout());
        blockLayers.add(RenderLayer.getTranslucent());
        // Adicione outros layers se necessário, como RenderLayer.getTripwire()

        // Loop pelos layers e chama nosso renderizador para cada um.
        for (RenderLayer layer : blockLayers) {
            BariumRenderManager.getInstance().renderLayer(matrices, layer, cameraX, cameraY, cameraZ);
        }

        // Como cancelamos o método original, ele espera que retornemos um valor do tipo SectionRenderState.
        // Retornar 'null' é a única opção segura. O código que chama `renderBlockLayers`
        // deve ser capaz de lidar com um valor nulo.
        cir.setReturnValue(null);
    }
}