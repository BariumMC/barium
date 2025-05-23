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
        // This inject point is not directly used for filtering in this implementation,
        // but it's kept for potential future use or debugging the iteration process.
        // The actual culling logic happens in barium$onRenderChunk.
    }
    
    // Inject at the point where a built chunk is checked if it can be drawn
    @Inject(method = "renderChunk", at = @At("HEAD"), cancellable = true)
    private void barium$onRenderChunk(BuiltChunk chunk, CallbackInfo ci) {
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