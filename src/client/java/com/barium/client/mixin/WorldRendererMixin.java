// --- Replace the content of: src/client/java/com/barium/client/mixin/WorldRendererMixin.java ---
package com.barium.client.mixin;

import com.barium.client.util.ChunkRenderManager;
import com.barium.config.BariumConfig;
import com.mojang.blaze3d.systems.RenderSystem; // Needed for FrameGraphBuilder
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Box;
import net.minecraft.util.profiler.Profiler; // Needed for renderMain
import org.joml.Matrix4f;
import org.joml.Vector4f; // Needed for the new render signature
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.buffers.GpuBufferSlice; // Needed for the new signatures
import java.util.BitSet;

@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin {

    @Shadow @Final private MinecraftClient client;

    /**
     * Passo 1: Preparação.
     * Injetamos no método 'render' usando a assinatura exata fornecida pelo log de erro.
     * O nosso objetivo é o mesmo: chamar nossa lógica de cálculo DEPOIS que o setupTerrain for chamado.
     */
    @Inject(
        // The method name to inject into
        method = "render(Lnet/minecraft/client/render/FrameGraphBuilder;Lnet/minecraft/client/render/RenderTickCounter;ZLnet/minecraft/client/render/Camera;Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;Lcom/mojang/blaze3d/buffers/GpuBufferSlice;Lorg/joml/Vector4f;Z)V",
        // The point of injection
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/render/WorldRenderer;setupTerrain(Lnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/Frustum;ZZ)V",
            shift = At.Shift.AFTER
        )
    )
    private void barium$captureFrustumAndPrepare(
            // Parameters of the target method, must match the signature exactly
            FrameGraphBuilder frameGraphBuilder,
            RenderTickCounter tickCounter,
            boolean hasWorld,
            Camera camera,
            Matrix4f projectionMatrix,
            Matrix4f positionMatrix,
            GpuBufferSlice fog,
            Vector4f skyColor,
            boolean renderBlockOutline,
            // The callback info object
            CallbackInfo ci
    ) {
        if (BariumConfig.C.ENABLE_FRUSTUM_CHUNK_CULLING && this.client != null) {
            // A lógica aqui depende de como obter o Frustum. O `setupTerrain` atualiza um campo interno.
            // Para maior compatibilidade, vamos injetar diretamente no `setupTerrain` como na solução anterior.
            // Esta injeção em `render` é muito complexa e frágil.
        }
    }

    /**
     * APROXIMAÇÃO MAIS SIMPLES E ROBUSTA (Revertendo para a solução anterior que estava quase correta)
     * Injetamos diretamente no `setupTerrain` para capturar o Frustum. Isso evita a assinatura complexa do método 'render'.
     */
    @Inject(method = "setupTerrain(Lnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/Frustum;ZZ)V", at = @At("HEAD"))
    private void barium$updateChunkRenderManager(Camera camera, Frustum frustum, boolean hasForcedFrustum, boolean spectator, CallbackInfo ci) {
        if (BariumConfig.C.ENABLE_FRUSTUM_CHUNK_CULLING && this.client != null) {
            ChunkRenderManager.getInstance().calculateChunksToRender(this.client, frustum);
        }
    }


    /**
     * Passo 2: Otimização com @Redirect no `renderMain`.
     * Esta parte permanece a mesma, pois sua lógica e assinaturas de alvo estão corretas.
     */
    @Redirect(
        method = "renderMain(Lnet/minecraft/client/render/FrameGraphBuilder;Lnet/minecraft/client/render/Frustum;Lnet/minecraft/client/render/Camera;Lorg/joml/Matrix4f;Lcom/mojang/blaze3d/buffers/GpuBufferSlice;ZZLnet/minecraft/client/render/RenderTickCounter;Lnet/minecraft/util/profiler/Profiler;)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/render/Frustum;isVisible(Lnet/minecraft/util/math/Box;)Z"
        )
    )
    private boolean barium$cullChunksWithBitSetRedirect(Frustum frustum, Box box) {
        if (!BariumConfig.C.ENABLE_FRUSTUM_CHUNK_CULLING) {
            return frustum.isVisible(box);
        }

        BitSet chunksToRenderBitSet = ChunkRenderManager.getChunksToRender();
        if (chunksToRenderBitSet == null) {
            return frustum.isVisible(box);
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