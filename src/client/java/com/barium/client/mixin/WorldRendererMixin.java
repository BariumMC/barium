// barium-1.21.5-devs/src/client/java/com/barium/client/mixin/WorldRendererMixin.java
package com.barium.client.mixin;

import com.barium.BariumMod;
import com.barium.client.optimization.GeometricOptimizer;
import com.barium.config.BariumConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.chunk.ChunkBuilder;
import net.minecraft.client.render.chunk.ChunkBuilder.BuiltChunk;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin para WorldRenderer para implementar oclusão culling e ganchos para renderização instanciada/impostor.
 * Baseado nos mappings Yarn 1.21.5+build.1.
 */
@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin {

    @Shadow private ChunkBuilder chunkBuilder;

    /**
     * Injeta no método render para executar o pre-pass de occlusion culling
     * antes que os chunks sejam iterados para renderização.
     */
    @Inject(
        method = "render(Lnet/minecraft/client/util/math/MatrixStack;FJZLnet/minecraft/client/render/Camera;" +
                 "Lnet/minecraft/client/render/GameRenderer;Lnet/minecraft/client/render/LightmapTextureManager;" +
                 "Lorg/joml/Matrix4f;)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/GameRenderer;getFov(Lnet/minecraft/client/render/Camera;FZ)D")
    )
    private void barium$beforeChunkRendering(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline,
                                             Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager,
                                             org.joml.Matrix4f positionMatrix, CallbackInfo ci) {
        if (BariumConfig.ENABLE_GEOMETRIC_OPTIMIZATION && BariumConfig.ENABLE_CHUNK_OCCLUSION_CULLING) {
            GeometricOptimizer.advanceFrame();
            BariumMod.LOGGER.debug("WorldRendererMixin: Pre-pass occlusion trigger point reached.");
        }
    }

    /**
     * Redireciona a chamada para renderizar um BuiltChunk para aplicar oclusão culling.
     */
    @Redirect(
        method = "render(Lnet/minecraft/client/util/math/MatrixStack;FJZLnet/minecraft/client/render/Camera;" +
                 "Lnet/minecraft/client/render/GameRenderer;Lnet/minecraft/client/render/LightmapTextureManager;" +
                 "Lorg/joml/Matrix4f;)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/chunk/ChunkBuilder$BuiltChunk;" +
                                           "render(Lnet/minecraft/client/render/RenderLayer;Lnet/minecraft/client/render/VertexConsumer;)V")
    )
    private void barium$redirectBuiltChunkRender(BuiltChunk instance, RenderLayer renderLayer, VertexConsumer consumer) {
        Camera currentCamera = MinecraftClient.getInstance().gameRenderer.getCamera();
        BlockPos chunkOrigin = instance.getOrigin();
        ChunkSectionPos chunkSectionPos = ChunkSectionPos.from(chunkOrigin);

        if (BariumConfig.ENABLE_GEOMETRIC_OPTIMIZATION && BariumConfig.ENABLE_CHUNK_OCCLUSION_CULLING &&
            GeometricOptimizer.shouldCullChunkSection(chunkSectionPos, currentCamera, MinecraftClient.getInstance().world)) {
            BariumMod.LOGGER.debug("WorldRendererMixin: Chunk section " + chunkSectionPos + " culled by occlusion.");
            return;
        }

        // Não cullado, prossegue com renderização.
        instance.render(renderLayer, consumer);
    }

    /**
     * Injeta no final do método render para chamar a renderização de instancing/impostors.
     */
    @Inject(
        method = "render(Lnet/minecraft/client/util/math/MatrixStack;FJZLnet/minecraft/client/render/Camera;" +
                 "Lnet/minecraft/client/render/GameRenderer;Lnet/minecraft/client/render/LightmapTextureManager;" +
                 "Lorg/joml/Matrix4f;)V",
        at = @At("RETURN")
    )
    private void barium$postRender(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline,
                                   Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager,
                                   org.joml.Matrix4f positionMatrix, CallbackInfo ci) {
        GeometricOptimizer.renderInstancedAndImpostorBlocks(MinecraftClient.getInstance().gameRenderer.getCamera());
    }
}
