package com.barium.client.mixin;

import com.barium.client.util.ChunkCullingUtils;
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
        BlockPos origin = this.getOrigin();
        MinecraftClient client = MinecraftClient.getInstance();

        // --- Otimização de Oclusão Total (Ideal para subsolo) ---
        if (BariumConfig.C.ENABLE_OCCLUSION_CULLING && client.world != null) {
            if (ChunkCullingUtils.isSectionTotallyOccluded(client.world, origin)) {
                cir.setReturnValue(false);
                return;
            }
        }
        
        // --- Otimização de Visibilidade por Flood-Fill COM "BOLHA DE SEGURANÇA" ---
        if (BariumConfig.C.ENABLE_FLOOD_FILL_CULLING) {
            // Apenas aplica a otimização se o jogador existir no mundo
            if (client.player != null) {
                int sectionX = origin.getX() >> 4;
                int sectionZ = origin.getZ() >> 4;

                int playerChunkX = client.player.getChunkPos().x;
                int playerChunkZ = client.player.getChunkPos().z;

                // Calcula a distância em chunks do jogador até a seção que estamos verificando
                int dx = Math.abs(playerChunkX - sectionX);
                int dz = Math.abs(playerChunkZ - sectionZ);
                
                // Define um raio de segurança (1 significa uma área de 3x3 chunks ao redor do jogador)
                int safetyRadius = 1;

                // A otimização SÓ É APLICADA se o chunk estiver FORA da bolha de segurança.
                if (dx > safetyRadius || dz > safetyRadius) {
                    int sectionY = origin.getY() >> 4;
                     if (!FloodFillVisibilityManager.getInstance().isSectionVisible(sectionX, sectionY, sectionZ)) {
                        cir.setReturnValue(false);
                        return; // Cancela a renderização do chunk distante
                    }
                }
                // Se o chunk estiver DENTRO da bolha, a verificação é pulada e ele será renderizado.
            }
        }

        // --- Otimização de Frustum Culling (Sua implementação existente) ---
        if (BariumConfig.C.ENABLE_FRUSTUM_CHUNK_CULLING) {
            final int chunkX = origin.getX() >> 4;
            final int chunkZ = origin.getZ() >> 4;
            if (!ChunkRenderManager.getInstance().isChunkInFrustum(chunkX, chunkZ)) {
                cir.setReturnValue(false);
                return;
            }
        }
    }
}