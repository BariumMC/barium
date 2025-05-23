package com.barium.client.mixin;

import com.barium.BariumMod;
import com.barium.client.optimization.ClientTerrainOptimizer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderLayer; // Import RenderLayer
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.chunk.ChunkBuilder.BuiltChunk;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.WorldChunk;
import org.joml.Matrix4f; // Import Matrix4f
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
// No longer need LocalCapture for the removed inject

@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin {

    @Shadow
    private MinecraftClient client;

    // The problematic 'barium$onRenderIterateChunks' is removed for simplicity and correctness.
    // The core culling logic happens directly at the renderChunk method.

    // Inject at the point where a built chunk is checked if it can be drawn
    // Corrected method signature for `renderChunk` (as of MC 1.21.5 Yarn mappings)
    @Inject(method = "renderChunk", at = @At("HEAD"), cancellable = true)
    private void barium$onRenderChunk(BuiltChunk chunk, RenderLayer renderLayer, MatrixStack matrices, double x, double y, double z, Matrix4f projectionMatrix, CallbackInfo ci) {
        // If the client world is null (e.g., during startup/shutdown) or optimization is disabled,
        // we don't apply culling and let Minecraft handle it normally.
        if (client.world == null || !com.barium.config.BariumConfig.ENABLE_TERRAIN_STREAMING) {
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
    // This mixin is specifically targeting the `builtChunk` local variable within `setupTerrain`.
    // The actual suppression of rebuild will be handled by the `BuiltChunkMixin`.
    @ModifyVariable(method = "setupTerrain", at = @At(value = "STORE", ordinal = 0), name = "builtChunk", allow = 1)
    private BuiltChunk barium$modifyBuiltChunkForRebuild(BuiltChunk builtChunk) {
        // The logic for whether a chunk should be rebuilt is now handled by the BuiltChunkMixin.
        // This method serves as a pass-through to ensure the BuiltChunk is processed by the later mixin.
        return builtChunk;
    }
}