package com.barium.client.mixin;

import com.barium.client.util.ChunkRenderManager;
import com.barium.client.util.FloodFillVisibilityManager;
import com.barium.config.BariumConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.chunk.ChunkBuilder;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChunkBuilder.BuiltChunk.class)
public abstract class ChunkRenderMixin {

    @Shadow public abstract BlockPos getOrigin();

    @Inject(method = "shouldBuild()Z", at = @At("HEAD"), cancellable = true)
    private void barium$onShouldBuild(CallbackInfoReturnable<Boolean> cir) {
        // Se não houver player, deixa o padrão do jogo
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;
        
        BlockPos origin = this.getOrigin();
        int chunkX = origin.getX() >> 4;
        int chunkZ = origin.getZ() >> 4;

        // 1. Flood Fill (Graph Culling) - O MAIS IMPORTANTE
        // Se o algoritmo de grafo diz que o chunk está ocluído (ex: caverna),
        // cancelamos imediatamente. Isso evita o frustum check e o build.
        if (BariumConfig.C.ENABLE_FLOOD_FILL_CULLING) {
            int sectionY = origin.getY() >> 4;
            
            // Bolha de segurança (3 chunks) ao redor do player para evitar glitches
            int pX = client.player.getChunkPos().x;
            int pZ = client.player.getChunkPos().z;
            
            if (Math.abs(pX - chunkX) > 1 || Math.abs(pZ - chunkZ) > 1) {
                // Verifica a visibilidade da seção específica (16x16x16)
                if (!FloodFillVisibilityManager.getInstance().isSectionVisible(chunkX, sectionY, chunkZ)) {
                    cir.setReturnValue(false);
                    return;
                }
            }
        }

        // 2. Frustum Culling (Visual)
        // Se passou pelo grafo (ou seja, existe caminho visual), verifica se está na câmera.
        if (BariumConfig.C.ENABLE_FRUSTUM_CHUNK_CULLING) {
            if (!ChunkRenderManager.getInstance().isChunkInFrustum(chunkX, chunkZ)) {
                cir.setReturnValue(false);
                return;
            }
        }
    }
}