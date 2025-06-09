package com.barium.client.mixin;

import com.barium.client.util.ChunkSectionUtils;
import com.barium.config.BariumConfig;
import net.minecraft.client.render.chunk.ChunkBuilder.BuiltChunk;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BuiltChunk.class)
public class BuiltChunkMixin {

    /**
     * Injeta no método que marca um chunk para reconstrução.
     * Se a otimização estiver ativa e a seção do chunk for vazia (apenas ar),
     * nós impedimos que ela seja marcada para reconstrução.
     */
    @Inject(
        method = "needsRebuild(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/BlockPos;)Z",
        at = @At("HEAD"),
        cancellable = true
    )
    private void barium$preventEmptyChunkRebuild(World world, BlockPos oldPos, BlockPos newPos, CallbackInfoReturnable<Boolean> cir) {
        if (!BariumConfig.ENABLE_EMPTY_CHUNK_CULLING) {
            return;
        }

        // Obtém a chunk e a seção correspondente à nova posição do BuiltChunk.
        Chunk chunk = world.getChunk(newPos);
        int sectionY = world.getSectionIndex(newPos.getY());

        // Precisamos garantir que o índice da seção seja válido.
        if (sectionY < world.getBottomSectionCoord() || sectionY >= world.getTopSectionCoord()) {
            // Se estiver fora dos limites do mundo, é vazio por definição.
            cir.setReturnValue(false);
            return;
        }

        ChunkSection section = chunk.getSection(sectionY);

        // Se a seção for vazia, nós forçamos o método a retornar 'false',
        // indicando que uma reconstrução não é necessária.
        if (ChunkSectionUtils.isSectionEmpty(section)) {
            cir.setReturnValue(false);
        }
    }
}