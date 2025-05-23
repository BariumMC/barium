package com.barium.client.mixin;

import com.barium.client.optimization.ClientTerrainOptimizer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.chunk.ChunkBuilder.BuiltChunk;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BuiltChunk.class)
public abstract class BuiltChunkMixin {

    @Shadow @Final protected BlockPos min; // 'min' is the correct shadow field name for the origin BlockPos in 1.21.5

    @Inject(method = "rebuild", at = @At("HEAD"), cancellable = true)
    private void barium$onRebuild(CallbackInfoReturnable<Runnable> cir) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (!com.barium.config.BariumConfig.ENABLE_CHUNK_LOD && !com.barium.config.BariumConfig.ENABLE_DIRECTIONAL_PRELOADING || client.world == null || client.player == null) {
            return;
        }

        // Corrected: Create ChunkPos from BlockPos
        ChunkPos chunkPos = new ChunkPos(min.getX() >> 4, min.getZ() >> 4); // Use bit shift for chunk coordinates
        // Or simply: ChunkPos chunkPos = new ChunkPos(min); // This constructor takes BlockPos directly

        WorldChunk worldChunk = client.world.getChunk(chunkPos.x, chunkPos.z);

        if (worldChunk != null) {
            int lod = ClientTerrainOptimizer.getChunkLOD(worldChunk, client.gameRenderer.getCamera());
            if (!ClientTerrainOptimizer.shouldRebuildChunkMesh(chunkPos, lod, client.player)) {
                cir.setReturnValue(null);
            }
        }
    }
}