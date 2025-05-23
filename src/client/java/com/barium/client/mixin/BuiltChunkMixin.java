package com.barium.client.mixin;

import com.barium.client.optimization.ClientTerrainOptimizer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.chunk.ChunkBuilder.BuiltChunk;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BuiltChunk.class)
public abstract class BuiltChunkMixin {

    // No need for @Shadow or @Accessor here, as we'll call a public method.
    // The BuiltChunk class (which this mixin applies to) already has a public `getOrigin()` method.

    @Inject(method = "rebuild", at = @At("HEAD"), cancellable = true)
    private void barium$onRebuild(CallbackInfoReturnable<Runnable> cir) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (!com.barium.config.BariumConfig.ENABLE_CHUNK_LOD && !com.barium.config.BariumConfig.ENABLE_DIRECTIONAL_PRELOADING || client.world == null || client.player == null) {
            return;
        }

        // Get the BuiltChunk instance (this) and call its public getOrigin() method.
        // The cast to BuiltChunk is often needed for the compiler when dealing with Mixin's `this`.
        BlockPos originPos = ((BuiltChunk)(Object)this).getOrigin();

        ChunkPos chunkPos = new ChunkPos(originPos.getX() >> 4, originPos.getZ() >> 4);

        WorldChunk worldChunk = client.world.getChunk(chunkPos.x, chunkPos.z);

        if (worldChunk != null) {
            int lod = ClientTerrainOptimizer.getChunkLOD(worldChunk, client.gameRenderer.getCamera());
            if (!ClientTerrainOptimizer.shouldRebuildChunkMesh(chunkPos, lod, client.player)) {
                cir.setReturnValue(null);
            }
        }
    }
}