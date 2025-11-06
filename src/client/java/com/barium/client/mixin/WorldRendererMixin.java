package com.barium.client.mixin;

import com.barium.client.optimization.ChunkOptimizer;
import com.barium.client.util.ChunkRenderManager;
import com.barium.client.util.ChunkVisibilityManager;
import com.barium.client.util.FloodFillVisibilityManager;
import com.barium.client.optimization.ChunkUploadThrottler;
import com.barium.config.BariumConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.render.chunk.ChunkBuilder;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin {

    public static Frustum capturedFrustum;

    @Shadow @Final private MinecraftClient client;
    @Shadow private ChunkBuilder chunkBuilder;

    @Inject(
        method = "render(Lnet/minecraft/client/util/math/MatrixStack;FLJZLnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/GameRenderer;Lnet/minecraft/client/render/LightmapTextureManager;Lorg/joml/Matrix4f;)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/WorldRenderer;setupTerrain(Lnet/minecraft/client/render/Camera;)V"),
        locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void barium$captureFrustumAndUpdateManagers(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f projectionMatrix, CallbackInfo ci, boolean bl, Frustum frustum) {
        capturedFrustum = frustum;

        if (client.world == null || client.player == null) {
            FloodFillVisibilityManager.getInstance().clear();
            ChunkVisibilityManager.getInstance().clear();
            ChunkRenderManager.getInstance().clear();
            return;
        }

        if (BariumConfig.C.ENABLE_FRUSTUM_CHUNK_CULLING) {
            ChunkRenderManager.getInstance().calculateChunksToRender(this.client, capturedFrustum);
        }
        if (BariumConfig.C.ENABLE_FLOOD_FILL_CULLING) {
            FloodFillVisibilityManager.getInstance().update(this.client);
        }
        if (BariumConfig.C.ENABLE_VISIBILITY_GRAPH_CULLING) {
            ChunkVisibilityManager.getInstance().update(this.client);
        }
    }

    @Inject(method = "updateChunks(Lnet/minecraft/client/render/Camera;)V", at = @At("HEAD"))
    private void barium$beforeUpdateChunks(Camera camera, CallbackInfo ci) {
        if (this.chunkBuilder != null) {
            // CORREÇÃO FINAL 25w45a: O método correto é getPos().
            this.chunkBuilder.setCameraPosition(camera.getPos());
        }
        ChunkUploadThrottler.resetCounter();
    }

    /**
     * CORREÇÃO FINAL 25w45a: A renderização de Block Entities mudou completamente.
     * A nova estratégia é interceptar o iterador da lista de entidades a serem renderizadas.
     * Nós filtramos essa lista com nossa lógica de culling e retornamos um novo iterador.
     * Isso é mais limpo e mais eficiente do que tentar cancelar cada chamada individualmente.
     */
    @Redirect(
        method = "renderBlockEntities(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;Lnet/minecraft/client/render/Camera;)V",
        at = @At(value = "INVOKE", target = "Ljava/util/List;iterator()Ljava/util/Iterator;")
    )
    private Iterator<BlockEntityRenderState> barium$cullBlockEntityList(List<BlockEntityRenderState> list, MatrixStack matrices, VertexConsumerProvider.Immediate vertexConsumers, Camera camera) {
        if (list.isEmpty()) {
            return list.iterator();
        }

        // Se ambas as opções de culling estiverem desativadas, retorna o iterador original sem processamento.
        if (!BariumConfig.C.ENABLE_BLOCK_ENTITY_CULLING && !BariumConfig.C.ENABLE_BLOCK_ENTITY_OCCLUSION_CULLING) {
            return list.iterator();
        }

        // Filtra a lista, mantendo apenas as entidades que devem ser renderizadas.
        List<BlockEntityRenderState> filteredList = list.stream()
            .filter(state -> {
                if (BariumConfig.C.ENABLE_BLOCK_ENTITY_CULLING && !ChunkOptimizer.shouldRenderBlockEntity(state.getBlockEntity(), camera)) {
                    return false; // Culling por distância
                }
                if (BariumConfig.C.ENABLE_BLOCK_ENTITY_OCCLUSION_CULLING && ChunkOptimizer.isBlockEntityOccluded(state.getBlockEntity(), camera)) {
                    return false; // Culling por oclusão
                }
                return true; // Manter na lista
            })
            .collect(Collectors.toList());

        // Retorna o iterador da nova lista filtrada.
        return filteredList.iterator();
    }
}