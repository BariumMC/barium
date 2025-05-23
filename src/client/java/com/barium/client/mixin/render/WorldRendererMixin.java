package com.barium.client.mixin.render;

import com.barium.Barium;
import com.barium.client.optimization.render.OcclusionManager;

import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.chunk.ChunkRendererRegion;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Iterator;
import java.util.List;

@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin {

    // Target the loop where chunkRendererRegions are iterated for rendering.
    // This injects *before* the actual rendering of each chunk.
    // The exact @At value might need fine-tuning based on MC version and other mods (Sodium).
    // Look for where `renderLayer` is called on a ChunkRendererRegion or where the ChunkRendererRegion itself is processed.

    // This Redirect targets the add method of the List iterator that holds renderable chunks.
    // We can then filter out chunks from this list before they are even considered for rendering.
    @Redirect(
            method = "render",
            at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", ordinal = 0),
            // The ordinal=0 is important if there are multiple list.add calls in the method.
            // We want the one where visible chunks are added to the list for rendering.
            // This is usually after frustum culling.
            remap = true // Ensure remapping for obfuscated names
    )
    private boolean barium$filterRenderableChunks(List<ChunkRendererRegion> list, Object chunkRendererRegionObject,
                                                    MatrixStack matrices, float tickDelta, long limitTime,
                                                    boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer,
                                                    LightmapTextureManager lightmapTextureManager, Matrix4f projectionMatrix) {

        ChunkRendererRegion chunkRendererRegion = (ChunkRendererRegion) chunkRendererRegionObject;

        // Perform our custom occlusion culling here
        // We pass the entire list of already frustum-culled chunks for the "intervening occluder" check.
        // Note: passing the whole list every time for each chunk can be slow for the "intervening occluder" check.
        // A more optimized approach would pre-process the occluders.
        if (OcclusionManager.isChunkOccluded(chunkRendererRegion, camera, list)) {
            // Barium.LOGGER.debug("Barium occluded chunk: {}", ((IChunkRendererRegion)chunkRendererRegion).barium$getRenderOrigin());
            return false; // Do not add to the list of chunks to render
        }

        // If not occluded by Barium, add it to the list as normal
        return list.add(chunkRendererRegion);
    }
}