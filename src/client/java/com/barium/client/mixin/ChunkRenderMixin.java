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

        // 1. Flood Fill (Graph Culling) - Melhorado para evitar bugs de carregamento lento
        if (BariumConfig.C.ENABLE_FLOOD_FILL_CULLING) {
            // Verifica se o chunk está marcado como visível no grafo
            if (!FloodFillVisibilityManager.getInstance().isChunkVisible(chunkX, chunkZ)) {
                // Em vez de bloquear totalmente, usamos uma estratégia de atualização esparsa
                int playerX = pX;
                int playerZ = pZ;
                int dx = Math.abs(playerX - chunkX);
                int dz = Math.abs(playerZ - chunkZ);

                int detailed = com.barium.config.BariumConfig.C.DETAILED_RENDER_RADIUS;
                int skipRate = Math.max(1, com.barium.config.BariumConfig.C.CHUNK_UPDATE_SKIP_RATE);
                int sparse = Math.max(1, com.barium.config.BariumConfig.C.SPARSE_CHUNK_FACTOR);

                // Mantém chunks muito próximos (raio 2) sempre atualizados para evitar problemas de loading
                if (dx <= 2 && dz <= 2) {
                    return; // deixa o método original rodar (retorna true)
                }

                // Se estiver dentro do raio detalhado, permitimos atualização normal
                if (dx <= detailed && dz <= detailed) {
                    return;
                }

                // Para chunks fora do raio detalhado: permitimos builds ocasionais baseados no tempo
                long worldTime = client.world.getTime();
                boolean onSparseGrid = (Math.floorMod(chunkX - playerX, sparse) == 0 && Math.floorMod(chunkZ - playerZ, sparse) == 0);

                if (onSparseGrid && (worldTime % skipRate == 0)) {
                    return; // permite rebuild ocasional
                }

                // Caso contrário, bloqueamos a construção por agora (mantendo o último estado renderizado)
                cir.setReturnValue(false);
                return;
            }
        }


        // 2. Frustum Culling (Campo de Visão)
        if (BariumConfig.C.ENABLE_FRUSTUM_CHUNK_CULLING) {
            if (!ChunkRenderManager.getInstance().isChunkInFrustum(chunkX, chunkZ)) {
                // Aplica lógica similar ao FloodFill: não bloqueia totalmente, apenas degrada atualização
                int playerX = pX;
                int playerZ = pZ;
                int dx = Math.abs(playerX - chunkX);
                int dz = Math.abs(playerZ - chunkZ);

                int detailed = com.barium.config.BariumConfig.C.DETAILED_RENDER_RADIUS;
                int skipRate = Math.max(1, com.barium.config.BariumConfig.C.CHUNK_UPDATE_SKIP_RATE);
                int sparse = Math.max(1, com.barium.config.BariumConfig.C.SPARSE_CHUNK_FACTOR);

                if (dx <= 2 && dz <= 2) {
                    return;
                }

                if (dx <= detailed && dz <= detailed) {
                    return;
                }

                long worldTime = client.world.getTime();
                boolean onSparseGrid = (Math.floorMod(chunkX - playerX, sparse) == 0 && Math.floorMod(chunkZ - playerZ, sparse) == 0);

                if (onSparseGrid && (worldTime % skipRate == 0)) {
                    return;
                }

                cir.setReturnValue(false);
                return;
            }
        }
    }
}