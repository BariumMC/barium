package com.barium.client.optimization;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.render.Camera;

public class ParticleOptimizer {

    private static final double RENDER_DISTANCE_SQUARED = 64 * 64;

    public static boolean shouldSkipParticleTick(Particle particle, Camera camera) {
        double dx = particle.x - camera.getPos().x;
        double dy = particle.y - camera.getPos().y;
        double dz = particle.z - camera.getPos().z;
        double distanceSquared = dx * dx + dy * dy + dz * dz;

        return distanceSquared > RENDER_DISTANCE_SQUARED;
    }

    public static boolean shouldRenderParticle(Particle particle, Camera camera) {
        double dx = particle.x - camera.getPos().x;
        double dy = particle.y - camera.getPos().y;
        double dz = particle.z - camera.getPos().z;
        double distanceSquared = dx * dx + dy * dy + dz * dz;

        return distanceSquared <= RENDER_DISTANCE_SQUARED;
    }
}
