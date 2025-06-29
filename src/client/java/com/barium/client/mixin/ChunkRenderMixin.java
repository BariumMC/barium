package com.barium.client.mixin;

import com.barium.client.util.ChunkRenderManager;
import com.barium.client.util.ChunkVisibilityManager;
import com.barium.config.BariumConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.chunk.ChunkBuilder;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChunkBuilder.BuiltChunk.class)
public abstract class ChunkRenderMixin {

    @Shadow public abstract BlockPos getOrigin();
    
    private static final long VISIBILITY_CHECK_DISTANCE_CHUNKS_SQ = 12 * 12;

    @Inject(method = "shouldBuild()Z", at = @At("HEAD"), cancellable = true)
    private void barium$onShouldBuild(CallbackInfoReturnable<Boolean> cir) {
        
        final BlockPos origin = this.getOrigin();
        final int chunkX = origin.getX() >> 4;
        final int chunkZ = origin.getZ() >> 4;
        
        // --- LÓGICA HÍBRIDA DE CULLING ---

        // Camada 1: Otimização Agressiva (Visibility Graph)
        if (BariumConfig.C.ENABLE_VISIBILITY_GRAPH_CULLING) {
            final var player = MinecraftClient.getInstance().player;
            if (player != null) {
                final ChunkPos cameraChunkPos = player.getChunkPos();

                final long dx = chunkX - cameraChunkPos.x;
                final long dz = chunkZ - cameraChunkPos.z;
                final long distSq = dx * dx + dz * dz;

                if (distSq > 4 && distSq < VISIBILITY_CHECK_DISTANCE_CHUNKS_SQ) {
                    if (!ChunkVisibilityManager.getInstance().isChunkPotentiallyVisible(chunkX, chunkZ)) {
                        cir.setReturnValue(false);
                        return;
                    }
                }
            }
        }
        
        // Camada 2: Otimização Padrão (Frustum Culling) - Nossa rede de segurança
        // CORREÇÃO: Toda a lógica complexa foi substituída por uma única chamada de método.
        if (BariumConfig.C.ENABLE_FRUSTUM_CHUNK_CULLING) {
            if (!ChunkRenderManager.getInstance().isChunkInFrustum(chunkX, chunkZ)) {
                cir.setReturnValue(false);
            }
        }
    }
}