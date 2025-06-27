package com.barium.client.optimization;

import net.minecraft.client.render.chunk.ChunkBuilder;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class ChunkRenderPrioritizer {
    private static Vec3d cameraPosition = Vec3d.ZERO;

    public static void updateCameraPosition(Vec3d newPosition) {
        cameraPosition = newPosition;
    }

    public static int compare(ChunkBuilder.BuiltChunk chunkA, ChunkBuilder.BuiltChunk chunkB) {
        BlockPos originA = chunkA.getOrigin();
        BlockPos originB = chunkB.getOrigin();
        double distSqA = cameraPosition.squaredDistanceTo(originA.getX(), originA.getY(), originA.getZ());
        double distSqB = cameraPosition.squaredDistanceTo(originB.getX(), originB.getY(), originB.getZ());
        return Double.compare(distSqA, distSqB);
    }
}