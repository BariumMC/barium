package com.barium.client.mixin;

import com.barium.client.optimization.ChunkOptimizer;
import com.barium.client.util.ChunkRenderManager;
import com.barium.client.util.ChunkVisibilityManager;
import com.barium.client.util.FloodFillVisibilityManager;
import com.barium.client.optimization.ChunkUploadThrottler;
import com.barium.config.BariumConfig;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
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

@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin {

    // CORREÇÃO 25w45a: O frustum não é mais um campo, então o capturamos do método render e o armazenamos aqui
    public static Frustum capturedFrustum;

    @Shadow @Final private MinecraftClient client;
    @Shadow private ChunkBuilder chunkBuilder;

    /**
     * CORREÇÃO 25w45a: O método `setupTerrain` foi removido.
     * Esta injeção agora captura o frustum e chama os managers de atualização do Barium
     * no início do método de renderização principal, que é o novo local correto.
     */
    @Inject(
        method = "render(Lnet/minecraft/client/util/math/MatrixStack;FLJZLnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/GameRenderer;Lnet/minecraft/client/render/LightmapTextureManager;Lorg/joml/Matrix4f;)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/render/Frustum;setPosition(DDD)V",
            shift = At.Shift.AFTER
        )
    )
    private void barium$captureFrustumAndUpdateManagers(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f projectionMatrix, CallbackInfo ci, Frustum frustum) {
        capturedFrustum = frustum; // Captura o frustum para outros mixins

        // Lógica do antigo `barium$updateAllChunkManagers`
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
            this.chunkBuilder.setCameraPosition(camera.getPos());
        }
        ChunkUploadThrottler.resetCounter();
    }

    /**
     * CORREÇÃO 25w45a: A classe `BlockEntityRenderDispatcher` foi removida.
     * Este @Redirect substitui a chamada de renderização de cada entidade de bloco,
     * permitindo-nos aplicar nossa lógica de culling (distância e oclusão) antes
     * que a entidade seja efetivamente enviada para a GPU.
     */
    @Redirect(
        method = "renderBlockEntities",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/block/entity/BlockEntityRenderer;render(Lnet/minecraft/block/entity/BlockEntity;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;II)V")
    )
    private void barium$advancedBlockEntityCullingRedirect(BlockEntityRenderer<BlockEntity> renderer, BlockEntity blockEntity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, Camera camera) {
        // Estágio 1: Culling por distância
        if (BariumConfig.C.ENABLE_BLOCK_ENTITY_CULLING) {
            if (!ChunkOptimizer.shouldRenderBlockEntity(blockEntity, camera)) {
                return; // Pula a renderização
            }
        }
       
        // Estágio 2: Culling por oclusão
        if (BariumConfig.C.ENABLE_BLOCK_ENTITY_OCCLUSION_CULLING) {
            if (ChunkOptimizer.isBlockEntityOccluded(blockEntity, camera)) {
                return; // Pula a renderização
            }
        }

        // Se passou em todas as verificações, renderiza normalmente
        renderer.render(blockEntity, tickDelta, matrices, vertexConsumers, light, overlay);
    }
}