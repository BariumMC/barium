package com.barium.client.optimization;

import com.barium.BariumMod;
import com.barium.config.BariumConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.render.Camera;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

/**
 * Otimiza o sistema de partículas, aplicando culling e LOD.
 */
public class ParticleOptimizer {

    private static final double MAX_DETAIL_DISTANCE_SQ = 32 * 32;
    private static final double MAX_RENDER_DISTANCE_SQ = 64 * 64;

    public static void init() {
        BariumMod.LOGGER.info("Inicializando ParticleOptimizer");
    }

    public static boolean shouldRenderParticle(Particle particle, Camera camera, ClientWorld world) {
        if (!BariumConfig.ENABLE_PARTICLE_OPTIMIZATION) {
            return true;
        }

        Vec3d cameraPos = camera.getPos();
        Box boundingBox = particle.getBoundingBox();

        if (boundingBox == null) {
            return true;
        }

        Vec3d particlePos = boundingBox.getCenter();
        double distanceSq = cameraPos.squaredDistanceTo(particlePos);

        if (distanceSq > MAX_RENDER_DISTANCE_SQ) {
            return false;
        }

        if (!isBoxInFrustum(boundingBox, camera)) {
            return false;
        }

        if (BariumConfig.ENABLE_PARTICLE_LOD && distanceSq > MAX_DETAIL_DISTANCE_SQ) {
            if ((world.getTime() + particle.hashCode()) % 2 != 0) {
                // Skip tick, but still render
            }
        }

        return true;
    }

    private static boolean isBoxInFrustum(Box box, Camera camera) {
        // TODO: Implement frustum culling properly
        return true;
    }

    public static boolean shouldSkipParticleTick(Particle particle, ClientWorld world) {
        if (!BariumConfig.ENABLE_PARTICLE_OPTIMIZATION || !BariumConfig.ENABLE_PARTICLE_LOD) {
            return false;
        }

        Camera camera = MinecraftClient.getInstance().gameRenderer.getCamera();
        Vec3d cameraPos = camera.getPos();
        Box boundingBox = particle.getBoundingBox();

        if (boundingBox == null) {
            return false;
        }

        Vec3d particlePos = boundingBox.getCenter();
        double distanceSq = cameraPos.squaredDistanceTo(particlePos);

        if (distanceSq > MAX_DETAIL_DISTANCE_SQ) {
            return (world.getTime() + particle.hashCode()) % 2 != 0;
        }

        return false;
    }
}
