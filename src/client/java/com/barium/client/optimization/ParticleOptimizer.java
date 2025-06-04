package com.barium.client.optimization;

import com.barium.client.mixin.ParticleAccessor;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.render.Camera;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public class ParticleOptimizer {

    private static final double MAX_RENDER_DISTANCE_SQ = 128 * 128;
    private static final double MAX_TICK_DISTANCE_SQ = 128 * 128;

    public static boolean shouldSkipTick(Particle particle, Camera camera) {
        ParticleAccessor accessor = (ParticleAccessor) particle;
        Vec3d particlePos = new Vec3d(accessor.getX(), accessor.getY(), accessor.getZ());
        Vec3d cameraPos = camera.getPos();

        double distanceSq = particlePos.squaredDistanceTo(cameraPos);
        return distanceSq > MAX_TICK_DISTANCE_SQ;
    }

    public static boolean shouldRender(Particle particle, Camera camera) {
        ParticleAccessor accessor = (ParticleAccessor) particle;
        Vec3d particlePos = new Vec3d(accessor.getX(), accessor.getY(), accessor.getZ());
        Vec3d cameraPos = camera.getPos();

        double distanceSq = particlePos.squaredDistanceTo(cameraPos);
        if (distanceSq > MAX_RENDER_DISTANCE_SQ) {
            return false;
        }

        Box box = new Box(
            accessor.getX() - 0.1, accessor.getY() - 0.1, accessor.getZ() - 0.1,
            accessor.getX() + 0.1, accessor.getY() + 0.1, accessor.getZ() + 0.1
        );

        return camera.getFrustum().isVisible(box);
    }
}
