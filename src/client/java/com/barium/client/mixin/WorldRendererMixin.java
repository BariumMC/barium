package com.barium.client.mixin;

import com.barium.BariumMod;
import com.barium.client.optimization.ClientTerrainOptimizer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.chunk.ChunkBuilder.BuiltChunk;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Iterator;
import java.util.SortedSet;

@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin {

    @Shadow
    private MinecraftClient client;

    // Inject before the main rendering loop of chunks to filter them
    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/Vec3d;distanceToSqr(Lnet/minecraft/util/math/Vec3d;)D"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void barium$onRenderIterateChunks(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, WorldRenderer.LightingProvider lightingProvider, CallbackInfo ci, Iterator<BuiltChunk> iterator) {
        if (!client.is  /// If the client is null, or not in a world. Or if the config is disabled
            // No need to check client.isIntegratedServerRunning() as this is client-side code.
            // Client.world can be null during startup or shutdown
            client.world == null || !com.barium.config.BariumConfig.ENABLE_TERRAIN_STREAMING) {
            return;
        }

        // We can't directly modify the iterator here, so we will filter within the loop
        // The more effective way to apply culling and LOD on rendering is within the
        // WorldRenderer.render method itself, by modifying the collection of BuiltChunks
        // or by cancelling the rendering of individual chunks if they are not supposed to be rendered.
        // We will modify the `canDraw` check for the chunk mesh.
    }
    
    // Inject at the point where a built chunk is checked if it can be drawn
    @Inject(method = "renderChunk", at = @At("HEAD"), cancellable = true)
    private void barium$onRenderChunk(BuiltChunk chunk, CallbackInfo ci) {
        if (!com.barium.config.BariumConfig.ENABLE_TERRAIN_STREAMING || client.world == null) {
            return;
        }
        
        // This mixin point is for a specific BuiltChunk. We need to derive the WorldChunk from it.
        // BuiltChunk does not expose the WorldChunk directly. However, it holds a ChunkPos.
        ChunkPos chunkPos = chunk.getOrigin().toChunkPos();
        WorldChunk worldChunk = client.world.getChunk(chunkPos.x, chunkPos.z);

        if (!ClientTerrainOptimizer.shouldRenderChunk(worldChunk, client.gameRenderer.getCamera())) {
            ci.cancel(); // Don't render this chunk
            // BariumMod.LOGGER.debug("Cancelled rendering for chunk: {}", chunkPos);
        }
    }
    
    // Modify the method that checks if a chunk should be rebuilt
    // This is the ideal place to inject LOD and directional preloading logic for meshing frequency.
    @ModifyVariable(method = "setupTerrain", at = @At(value = "STORE", ordinal = 0), name = "builtChunk", allow = 1)
    private BuiltChunk barium$modifyBuiltChunkForRebuild(BuiltChunk builtChunk) {
        if (!com.barium.config.BariumConfig.ENABLE_CHUNK_LOD && !com.barium.config.BariumConfig.ENABLE_DIRECTIONAL_PRELOADING) {
            return builtChunk; // No optimization applied
        }

        // Get the chunk's WorldChunk from its position
        ChunkPos chunkPos = builtChunk.getOrigin().toChunkPos();
        WorldChunk worldChunk = client.world.getChunk(chunkPos.x, chunkPos.z);
        
        if (worldChunk != null) {
            // Determine LOD level for this chunk
            int lod = ClientTerrainOptimizer.getChunkLOD(worldChunk, client.gameRenderer.getCamera());
            
            // Check if it should rebuild its mesh based on LOD and player movement
            if (!ClientTerrainOptimizer.shouldRebuildChunkMesh(chunkPos, lod, client.player)) {
                // If not, ensure it doesn't get added to the list of chunks to be rebuilt,
                // effectively skipping the re-meshing process for this frame/tick.
                // Returning null here could cause issues if the original code doesn't handle null.
                // A safer way would be to modify the collection itself or use a different inject point.
                // As a workaround, we can ensure the builtChunk does not have its pending rebuild flag set.
                // However, there's no direct flag access here.
                // The current BuiltChunkMixin will handle the actual rebuild suppression.
            }
        }
        return builtChunk;
    }

    // Another entry point for modifying chunk rebuild priority or frequency
    // This targets the iteration of `BuiltChunk` objects to be rebuilt.
    // It's tricky to directly modify the `chunkBatcher.rebuildChunks` queue.
    // A better approach is to let the `BuiltChunkMixin` decide if a chunk should rebuild.
}