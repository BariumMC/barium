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
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

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
            // CORREÇÃO 25w45a: Camera.getPos() foi removido. Use camera.getPosition().
            this.chunkBuilder.setCameraPosition(camera.getPosition());
        }
        ChunkUploadThrottler.resetCounter();
    }

    /**
     * CORREÇÃO 25w45a: A classe `BlockEntityRenderDispatcher` foi removida e a assinatura do renderizador mudou.
     * Este @Redirect agora usa um tipo genérico <T> para funcionar com qualquer tipo de BlockEntity.
     */
    @Redirect(
        method = "renderBlockEntities(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;Lnet/minecraft/client/render/Camera;)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/block/entity/BlockEntityRenderer;render(Lnet/minecraft/block/entity/BlockEntity;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;II)V")
    )
    private <T extends BlockEntity> void barium$advancedBlockEntityCullingRedirect(BlockEntityRenderer<T> renderer, T blockEntity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, MatrixStack capturedMatrices, VertexConsumerProvider.Immediate capturedVertexConsumers, Camera camera) {
        if (BariumConfig.C.ENABLE_BLOCK_ENTITY_CULLING && !ChunkOptimizer.shouldRenderBlockEntity(blockEntity, camera)) {
            return;
        }
       
        if (BariumConfig.C.ENABLE_BLOCK_ENTITY_OCCLUSION_CULLING && ChunkOptimizer.isBlockEntityOccluded(blockEntity, camera)) {
            return;
        }

        renderer.render(blockEntity, tickDelta, matrices, vertexConsumers, light, overlay);
    }
}