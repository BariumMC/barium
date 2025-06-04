package com.barium.client.optimization;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.render.Camera;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public class ParticleOptimizer {

    // Distância máxima para atualizar e renderizar partículas
    private static final double MAX_UPDATE_DISTANCE_SQR = 128 * 128;
    private static final double MAX_RENDER_DISTANCE_SQR = 128 * 128;

    // Verifica se deve pular o tick da partícula
    public static boolean shouldSkipParticleTick(Particle particle, Camera camera) {
        Vec3d particlePos = new Vec3d(particle.x, particle.y, particle.z);
        Vec3d cameraPos = camera.getPos();
        double distanceSq = particlePos.squaredDistanceTo(cameraPos);
        return distanceSq > MAX_UPDATE_DISTANCE_SQR;
    }

    // Verifica se deve renderizar a partícula
    public static boolean shouldRenderParticle(Particle particle, Camera camera) {
        Vec3d particlePos = new Vec3d(particle.x, particle.y, particle.z);
        Vec3d cameraPos = camera.getPos();
        double distanceSq = particlePos.squaredDistanceTo(cameraPos);

        if (distanceSq > MAX_RENDER_DISTANCE_SQR) {
            return false;
        }

        // Frustum culling simplificado
        Box box = new Box(
            particle.x - 0.1, particle.y - 0.1, particle.z - 0.1,
            particle.x + 0.1, particle.y + 0.1, particle.z + 0.1
        );
        return camera.isBoundingBoxInFrustum(box);
    }
}
