package com.barium.client.mixin;

import com.barium.client.util.ChunkRenderManager;
import com.barium.client.util.FloodFillVisibilityManager;
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

    @Inject(method = "shouldBuild()Z", at = @At("HEAD"), cancellable = true)
    private void barium$onShouldBuild(CallbackInfoReturnable<Boolean> cir) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;
        
        BlockPos origin = this.getOrigin();
        int chunkX = origin.getX() >> 4;
        int chunkZ = origin.getZ() >> 4;

        ChunkPos playerChunkPos = client.player.getChunkPos();
        int pX = playerChunkPos.x;
        int pZ = playerChunkPos.z;

        // --- CORREÇÃO CRÍTICA PARA TELA DE LOADING INFINITA ---
        // Se o chunk estiver muito perto do jogador (Raio de 2 chunks / 32 blocos),
        // NUNCA aplique culling. O Minecraft precisa desses chunks para sair da tela de loading.
        if (Math.abs(pX - chunkX) <= 2 && Math.abs(pZ - chunkZ) <= 2) {
            return; // Deixa o método original rodar (retorna true)
        }
        // -------------------------------------------------------

        // 1. Flood Fill (Graph Culling)
        if (BariumConfig.C.ENABLE_FLOOD_FILL_CULLING) {
            int sectionY = origin.getY() >> 4;
            // Verifica se a seção está marcada como visível no grafo
            if (!FloodFillVisibilityManager.getInstance().isSectionVisible(chunkX, sectionY, chunkZ)) {
                cir.setReturnValue(false);
                return;
            }
        }

        // 2. Frustum Culling (Campo de Visão)
        if (BariumConfig.C.ENABLE_FRUSTUM_CHUNK_CULLING) {
            if (!ChunkRenderManager.getInstance().isChunkInFrustum(chunkX, chunkZ)) {
                cir.setReturnValue(false);
                return;
            }
        }
    }
}