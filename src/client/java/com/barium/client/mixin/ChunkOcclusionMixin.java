package com.barium.client.mixin;

import com.barium.client.optimization.ChunkOcclusionOptimizer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.chunk.ChunkBuilder;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Frustum;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(WorldRenderer.class)
public abstract class ChunkOcclusionMixin {

    @Redirect(
        method = "setupTerrain(Lnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/Frustum;ZZ)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/render/Frustum;isVisible(Lnet/minecraft/util/math/Box;)Z"
        )
    )
    private boolean barium$advancedOcclusionCheck(Frustum frustumInstance, net.minecraft.util.math.Box box, Camera camera, Frustum setupTerrainFrustum, boolean hasForcedFrustum, boolean spectator) {
        boolean vanillaVisible = frustumInstance.isVisible(box);
        if (!vanillaVisible) {
            return false;
        }

        ChunkBuilder.BuiltChunk chunk = null; // Placeholder - How to get the chunk here?

        if (chunk != null && !ChunkOcclusionOptimizer.shouldRenderChunkSection(chunk, camera)) {
             return false;
        }

        return true;
    }
}