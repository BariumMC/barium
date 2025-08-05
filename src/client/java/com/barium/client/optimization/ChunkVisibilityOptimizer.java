package com.barium.client.optimization;

import net.minecraft.client.render.Camera;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Box;

/**
 * Otimizador de visibilidade de chunk usando lógica semelhante à usada no Sodium.
 */
public class ChunkVisibilityOptimizer {

    private static final double EPSILON = 1e-4;
    private static final double MAX_RENDER_DISTANCE_SQUARED = 1024 * 1024; // 1024 blocos

    /**
     * Verifica se um chunk (definido pela bounding box) está visível com base na posição da câmera.
     * Usa margem de erro epsilon para maior estabilidade em ponto flutuante.
     */
    public static boolean isChunkVisible(Box boundingBox, Camera camera) {
        Vec3d camPos = camera.getPos();

        double dx = adjustEpsilon(nearestToZero(boundingBox.minX + 1.0) - camPos.x);
        double dy = adjustEpsilon(nearestToZero(boundingBox.minY + 1.0) - camPos.y);
        double dz = adjustEpsilon(nearestToZero(boundingBox.minZ + 1.0) - camPos.z);

        double distanceSq = dx * dx + dy * dy + dz * dz;

        return distanceSq <= MAX_RENDER_DISTANCE_SQUARED;
    }

    private static double adjustEpsilon(double value) {
        return Math.abs(value) < EPSILON ? 0.0 : value;
    }

    private static double nearestToZero(double value) {
        return Math.floor(value) + 0.5;
    }
}