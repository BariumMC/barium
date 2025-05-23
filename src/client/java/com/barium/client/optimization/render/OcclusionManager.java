package com.barium.client.optimization.render;

import com.barium.Barium;
import com.barium.client.mixin.IChunkRendererRegion;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.chunk.ChunkRendererRegion;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class OcclusionManager {

    // Configurações para definir o que é "principalmente opaco"
    private static final double OPAQUE_BLOCK_THRESHOLD = 0.8; // 80% dos blocos para ser considerado opaco
    private static final int CHUNK_SECTION_SIZE = 16;

    /**
     * Calculates and sets the occlusion metadata for a given chunk renderer region.
     * This method is called during chunk mesh generation.
     *
     * @param world The world instance.
     * @param region The ChunkRendererRegion to analyze.
     */
    public static void computeOcclusionData(World world, ChunkRendererRegion region) {
        if (!(region instanceof IChunkRendererRegion bariumRegion)) {
            return;
        }

        // Store the render origin
        bariumRegion.barium$setRenderOrigin(new BlockPos(region.getMinX(), region.getMinY(), region.getMinZ()));

        int opaqueBlocksCount = 0;
        int totalBlocks = CHUNK_SECTION_SIZE * CHUNK_SECTION_SIZE * CHUNK_SECTION_SIZE;

        // Calculate potential visible faces mask (0-5 for N, S, E, W, Up, Down)
        // Bit 0: South (+Z)
        // Bit 1: North (-Z)
        // Bit 2: East (+X)
        // Bit 3: West (-X)
        // Bit 4: Up (+Y)
        // Bit 5: Down (-Y)
        int visibleFacesMask = 0;

        BlockPos.Mutable mutablePos = new BlockPos.Mutable();

        // Iterate through the chunk section's 16x16x16 volume
        for (int x = 0; x < CHUNK_SECTION_SIZE; x++) {
            for (int y = 0; y < CHUNK_SECTION_SIZE; y++) {
                for (int z = 0; z < CHUNK_SECTION_SIZE; z++) {
                    mutablePos.set(region.getMinX() + x, region.getMinY() + y, region.getMinZ() + z);
                    BlockState state = world.getBlockState(mutablePos);

                    // Check for general opaqueness
                    if (state.isOpaqueFullCube(world, mutablePos) && !state.isOf(Blocks.WATER) && !state.isOf(Blocks.LAVA)) {
                        opaqueBlocksCount++;
                    }

                    // Check faces for potential visibility (simplified)
                    // If a block on the edge is NOT opaque, that face is potentially visible
                    if (x == 0 && !state.isOpaqueFullCube(world, mutablePos)) visibleFacesMask |= (1 << 3); // West
                    if (x == CHUNK_SECTION_SIZE - 1 && !state.isOpaqueFullCube(world, mutablePos)) visibleFacesMask |= (1 << 2); // East
                    if (y == 0 && !state.isOpaqueFullCube(world, mutablePos)) visibleFacesMask |= (1 << 5); // Down
                    if (y == CHUNK_SECTION_SIZE - 1 && !state.isOpaqueFullCube(world, mutablePos)) visibleFacesMask |= (1 << 4); // Up
                    if (z == 0 && !state.isOpaqueFullCube(world, mutablePos)) visibleFacesMask |= (1 << 1); // North
                    if (z == CHUNK_SECTION_SIZE - 1 && !state.isOpaqueFullCube(world, mutablePos)) visibleFacesMask |= (1 << 0); // South
                }
            }
        }

        boolean isMostlyOpaque = (double) opaqueBlocksCount / totalBlocks >= OPAQUE_BLOCK_THRESHOLD;
        bariumRegion.barium$setMostlyOpaque(isMostlyOpaque);

        // If the chunk is fully opaque, no faces are visible from outside.
        // If it's not fully opaque, assume all faces are potentially visible unless specifically blocked.
        if (isMostlyOpaque) {
            bariumRegion.barium$setVisibleFacesMask(0); // Fully opaque, no faces visible from outside
        } else {
            // This is a simplification. A truly robust system would need to check each face more thoroughly.
            // For now, if it's not mostly opaque, assume all faces are potentially visible, or use the calculated mask.
            // Let's use the calculated mask for better granularity.
            bariumRegion.barium$setVisibleFacesMask(visibleFacesMask);
        }

        // Barium.LOGGER.debug("Chunk region {} ({} blocks) is mostly opaque: {}. Visible faces mask: {}",
        //         bariumRegion.barium$getRenderOrigin(), opaqueBlocksCount, isMostlyOpaque, Integer.toBinaryString(visibleFacesMask));
    }

    /**
     * Determines if a ChunkRendererRegion should be occluded based on our custom logic.
     * This is called AFTER the standard Frustum Culling.
     *
     * @param region The ChunkRendererRegion to test.
     * @param camera The current camera position and orientation.
     * @param renderChunks The list of all ChunkRendererRegions that passed frustum culling.
     *                     (This list is used for portal-like culling by checking for intervening occluders)
     * @return True if the chunk should be occluded (not rendered), false otherwise.
     */
    public static boolean isChunkOccluded(ChunkRendererRegion region, Camera camera, Iterable<ChunkRendererRegion> renderChunks) {
        if (!(region instanceof IChunkRendererRegion bariumRegion)) {
            return false; // Cannot apply culling if interface not implemented
        }

        // --- Software-based Occlusion Map ---
        // If the chunk is fully opaque and the camera is inside its bounding box, don't occlude.
        // We need to see what's around us even if we're inside an opaque chunk.
        if (bariumRegion.barium$isMostlyOpaque()) {
            Box boundingBox = region.getBoundingBox();
            Vec3d cameraPos = camera.getPos();
            if (boundingBox.contains(cameraPos)) {
                return false; // Camera is inside an opaque chunk, render it.
            }
        }

        // --- Simplified Portal-based / Intervening Occluder Culling ---
        // If the current chunk is mostly opaque, it could act as an occluder for chunks behind it.
        // If a *target* chunk is mostly opaque, and there's another *opaque* chunk directly between
        // the camera and the target, then the target might be occluded.

        Vec3d cameraPos = camera.getPos();
        Vec3d chunkCenter = region.getBoundingBox().getCenter();
        Vec3d direction = chunkCenter.subtract(cameraPos).normalize();

        // Determine which face of the current chunk is facing the camera
        // This is a rough estimate for our simplified portal culling
        int facingFaceMask = 0;
        if (direction.x > 0.5) facingFaceMask |= (1 << 3); // Camera is to the West, looking East
        else if (direction.x < -0.5) facingFaceMask |= (1 << 2); // Camera is to the East, looking West
        if (direction.y > 0.5) facingFaceMask |= (1 << 5); // Camera is below, looking Up
        else if (direction.y < -0.5) facingFaceMask |= (1 << 4); // Camera is above, looking Down
        if (direction.z > 0.5) facingFaceMask |= (1 << 1); // Camera is North, looking South
        else if (direction.z < -0.5) facingFaceMask |= (1 << 0); // Camera is South, looking North

        // Check if the relevant face of this chunk is marked as "not visible" (i.e., fully occluded by inner structure)
        // If our visibleFacesMask says this specific face is blocked, then it's occluded from this direction.
        if (bariumRegion.barium$getVisibleFacesMask() != 0 && (bariumRegion.barium$getVisibleFacesMask() & facingFaceMask) == 0) {
            // This chunk's relevant face is not marked as visible.
            // This means from the camera's perspective, this chunk is likely fully blocked internally.
            // Example: looking into a mountain from the outside, and this chunk is deep inside with no exterior openings.
            // This is a *strong* candidate for occlusion.
            // Barium.LOGGER.debug("Occluding chunk {} based on internal face visibility mask: {}", bariumRegion.barium$getRenderOrigin(), Integer.toBinaryString(facingFaceMask));
            return true;
        }


        // Advanced Culling: Check for intervening opaque chunks
        // This is where a very simplified "portal-based" idea comes in.
        // If an opaque chunk is between the camera and the target chunk, occlude the target.
        // This is computationally expensive if done for *every* pair, so we'll simplify.
        // Iterate through *other* chunks that already passed frustum culling.
        // If another *opaque* chunk is significantly closer and lies on the ray to the current chunk, it's occluded.
        for (ChunkRendererRegion otherRegion : renderChunks) {
            if (otherRegion == region) continue; // Don't check against itself

            if (otherRegion instanceof IChunkRendererRegion otherBariumRegion && otherBariumRegion.barium$isMostlyOpaque()) {
                // Heuristic: Is the 'otherRegion' between the camera and the 'region'?
                // A very crude way: check if the 'otherRegion' bounding box intersects the ray from camera to 'region' center.
                // Or simply if it's closer and its center is roughly in the same direction.

                Box otherBox = otherRegion.getBoundingBox();
                double distToOther = cameraPos.distanceTo(otherBox.getCenter());
                double distToCurrent = cameraPos.distanceTo(chunkCenter);

                if (distToOther < distToCurrent - 1.0 && otherBox.intersects(cameraPos, chunkCenter)) {
                    // Check if the other region actually blocks the view.
                    // This is a simple AABB intersection check with the ray, which is not perfect but a start.
                    // A proper check would require raycasting.
                    // If 'otherRegion' is mostly opaque AND it appears to be between us and 'region',
                    // then 'region' might be occluded.
                    // This is a significant performance bottleneck if not optimized (e.g., using a spatial hash for occluders).
                    // For now, let's keep it simple: if *any* mostly opaque chunk is closer and in the general direction, occlude.
                    // Barium.LOGGER.debug("Occluding chunk {} due to intervening occluder {}", bariumRegion.barium$getRenderOrigin(), otherBariumRegion.barium$getRenderOrigin());
                    return true;
                }
            }
        }


        return false; // Not occluded by our custom logic
    }
}