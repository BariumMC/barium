package com.barium.client.mixin.render;

import com.barium.Barium;
import com.barium.client.optimization.render.OcclusionManager;
import net.minecraft.block.BlockState;
import net.minecraft.block.FluidBlock;
import net.minecraft.client.render.chunk.ChunkBuilder;
import net.minecraft.client.render.chunk.ChunkRendererRegion;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;

@Mixin(ChunkBuilder.Task.class)
public abstract class ChunkBuilderMixin {

    // Shadow a field or method from the outer ChunkBuilder.Task class if needed.
    // For example, to get the ChunkRendererRegion instance.
    @Shadow(remap = false) @Final ChunkRendererRegion chunkRendererRegion; // Access the chunkRendererRegion field
    @Shadow(remap = false) @Final BlockPos origin; // Access the origin field

    @Inject(method = "buildLayer", at = @At("RETURN"))
    private void barium$onBuildLayer(CallbackInfoReturnable<Set<FluidBlock>> cir) {
        // This method is called after the chunk mesh for a layer (e.g., Solid, Cutout, Translucent) is built.
        // We want to calculate our occlusion data based on the full chunk section.
        // It's crucial that this doesn't run excessively, but buildLayer is only called when a chunk needs rebuilding.

        // Access the world from the ChunkRendererRegion or ChunkBuilder itself.
        // ChunkRendererRegion holds a reference to the world.
        World world = chunkRendererRegion.getWorld();
        if (world != null) {
            OcclusionManager.computeOcclusionData(world, chunkRendererRegion);
        } else {
            Barium.LOGGER.warn("World is null during ChunkBuilder.Task.buildLayer for chunk at {}", origin);
        }
    }
}