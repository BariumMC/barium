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
        BlockPos origin = this.getOrigin();
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        // Verificação 1: Otimização de Frustum (a mais barata e rápida)
        // Se o chunk inteiro não está no campo de visão da câmera, pulamos a reconstrução.
        if (BariumConfig.C.ENABLE_FRUSTUM_CHUNK_CULLING) {
            final int chunkX = origin.getX() >> 4;
            final int chunkZ = origin.getZ() >> 4;
            if (!ChunkRenderManager.getInstance().isChunkInFrustum(chunkX, chunkZ)) {
                cir.setReturnValue(false);
                return;
            }
        }
        
        // Verificação 2: Otimização de Flood-Fill (verificação rápida com dados assíncronos)
        // Usa dados pré-calculados para determinar a visibilidade. A verificação em si é muito rápida.
        if (BariumConfig.C.ENABLE_FLOOD_FILL_CULLING) {
            int sectionX = origin.getX() >> 4;
            int sectionY = origin.getY() >> 4;
            int sectionZ = origin.getZ() >> 4;

            // A "bolha de segurança" impede que a otimização remova chunks muito próximos,
            // evitando "buracos" visuais ao se virar rapidamente. O raio foi aumentado para mais estabilidade.
            int playerChunkX = client.player.getChunkPos().x;
            int playerChunkZ = client.player.getChunkPos().z;
            int safetyRadius = 2; // Raio de segurança aumentado para uma área de 5x5 chunks.

            // A otimização só é aplicada se o chunk estiver FORA da bolha de segurança.
            if (Math.abs(playerChunkX - sectionX) > safetyRadius || Math.abs(playerChunkZ - sectionZ) > safetyRadius) {
                 if (!FloodFillVisibilityManager.getInstance().isSectionVisible(sectionX, sectionY, sectionZ)) {
                    cir.setReturnValue(false);
                    return;
                }
            }
        }

        // A verificação síncrona de oclusão total (`isSectionTotallyOccluded`) foi REMOVIDA deste método.
        // A sua execução aqui era muito cara e causava a lentidão no carregamento de chunks.
        // A otimização Flood-Fill já lida com a maioria dos casos de oclusão de forma muito mais eficiente.
    }
}