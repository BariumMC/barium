// --- Substitua o conteúdo em: src/client/java/com/barium/client/mixin/WorldRendererMixin.java ---
package com.barium.client.mixin;

import com.barium.client.util.ChunkRenderManager;
import com.barium.config.BariumConfig;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.ObjectAllocator;
import net.minecraft.util.math.Box;
import net.minecraft.util.profiler.Profiler;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.BitSet;

@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin {

    @Shadow @Final private MinecraftClient client;
    @Shadow private Frustum frustum; // Adicionamos uma sombra para acessar o frustum após ele ser atualizado

    /**
     * Passo 1: Preparação.
     * Injetamos no método 'render' usando a assinatura exata para 1.21.6.
     * Nosso ponto de injeção é logo após a chamada a 'setupTerrain', garantindo que
     * o campo 'this.frustum' esteja atualizado para o frame atual.
     */
    @Inject(
        // Usamos a assinatura exata que você forneceu.
        method = "render(Lnet/minecraft/client/util/ObjectAllocator;Lnet/minecraft/client/render/RenderTickCounter;ZLnet/minecraft/client/render/Camera;Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;Lcom/mojang/blaze3d/buffers/GpuBufferSlice;Lorg/joml/Vector4f;Z)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/render/WorldRenderer;setupTerrain(Lnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/Frustum;ZZ)V",
            shift = At.Shift.AFTER
        )
    )
    private void barium$prepareVisibleChunkSet(
            // Parâmetros do método 'render' (devem corresponder à assinatura)
            ObjectAllocator allocator,
            RenderTickCounter tickCounter,
            boolean renderBlockOutline,
            Camera camera,
            Matrix4f positionMatrix,
            Matrix4f projectionMatrix,
            GpuBufferSlice fog,
            Vector4f fogColor,
            boolean shouldRenderSky,
            // Objeto de callback da injeção
            CallbackInfo ci
    ) {
        if (BariumConfig.C.ENABLE_FRUSTUM_CHUNK_CULLING && this.client != null) {
            // Agora que setupTerrain foi chamado, 'this.frustum' está pronto para uso.
            ChunkRenderManager.getInstance().calculateChunksToRender(this.client, this.frustum);
        }
    }

    /**
     * Passo 2: Otimização com @Redirect no `renderMain`.
     * Esta parte já estava correta e vai funcionar agora que a preparação é feita corretamente.
     */
    @Redirect(
        method = "renderMain(Lnet/minecraft/client/render/FrameGraphBuilder;Lnet/minecraft/client/render/Frustum;Lnet/minecraft/client/render/Camera;Lorg/joml/Matrix4f;Lcom/mojang/blaze3d/buffers/GpuBufferSlice;ZZLnet/minecraft/client/render/RenderTickCounter;Lnet/minecraft/util/profiler/Profiler;)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/render/Frustum;isVisible(Lnet/minecraft/util/math/Box;)Z"
        )
    )
    private boolean barium$cullChunksWithBitSetRedirect(Frustum frustumInstance, Box box) {
        if (!BariumConfig.C.ENABLE_FRUSTUM_CHUNK_CULLING) {
            return frustumInstance.isVisible(box);
        }

        BitSet chunksToRenderBitSet = ChunkRenderManager.getChunksToRender();
        if (chunksToRenderBitSet == null) {
            return frustumInstance.isVisible(box);
        }

        final int minChunkX = ChunkRenderManager.getMinRenderChunkX();
        final int minChunkZ = ChunkRenderManager.getMinRenderChunkZ();
        final int gridSize = ChunkRenderManager.getRenderGridSize();

        final int chunkX = (int) box.minX >> 4;
        final int chunkZ = (int) box.minZ >> 4;

        final int localX = chunkX - minChunkX;
        final int localZ = chunkZ - minChunkZ;

        if (localX < 0 || localX >= gridSize || localZ < 0 || localZ >= gridSize) {
            return false;
        }

        final int chunkIndex = localX + localZ * gridSize;
        
        return chunksToRenderBitSet.get(chunkIndex);
    }
}