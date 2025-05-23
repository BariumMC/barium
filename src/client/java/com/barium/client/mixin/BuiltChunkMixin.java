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

    // Corrected field name from "origin" to "min" for 1.21.5 mappings
    @Shadow @Final protected BlockPos min;

    // Intercept the request to rebuild the chunk mesh
    // This is called by ChunkBatcher to get a Runnable task for rebuilding.
    @Inject(method = "rebuild", at = @At("HEAD"), cancellable = true)
    private void barium$onRebuild(CallbackInfoReturnable<Runnable> cir) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (!com.barium.config.BariumConfig.ENABLE_CHUNK_LOD && !com.barium.config.BariumConfig.ENABLE_DIRECTIONAL_PRELOADING || client.world == null || client.player == null) {
            return;
        }

        // Use 'min' instead of 'origin'
        ChunkPos chunkPos = min.toChunkPos();
        WorldChunk worldChunk = client.world.getChunk(chunkPos.x, chunkPos.z);

        if (worldChunk != null) {
            int lod = ClientTerrainOptimizer.getChunkLOD(worldChunk, client.gameRenderer.getCamera());
            if (!ClientTerrainOptimizer.shouldRebuildChunkMesh(chunkPos, lod, client.player)) {
                // If we shouldn't rebuild, cancel the original method.
                // We return null as a Runnable, which means no rebuild task is submitted.
                // This is a powerful cancellation.
                cir.setReturnValue(null);
            }
        }
    }
}