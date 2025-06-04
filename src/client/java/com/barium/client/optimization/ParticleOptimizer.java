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
 * Baseado nos mappings Yarn 1.21.5+build.1
 */
public class ParticleOptimizer {

    private static final double MAX_DETAIL_DISTANCE_SQ = 32 * 32;
    private static final double MAX_RENDER_DISTANCE_SQ = 64 * 64;

    /**
     * Inicializa o otimizador de partículas.
     */
    public static void init() {
        BariumMod.LOGGER.info("Inicializando ParticleOptimizer");
    }

    /**
     * Verifica se uma partícula deve ser renderizada com base na distância e visibilidade.
     */
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

        return true;
    }

    /**
     * Verifica se um Bounding Box está dentro do frustum da câmera.
     */
    private static boolean isBoxInFrustum(Box box, Camera camera) {
        return camera.getFrustum().isVisible(box);
    }

    /**
     * Verifica se o tick de uma partícula deve ser pulado com base na distância (LOD progressivo).
     */
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

        long frame = world.getTime() + particle.hashCode();

        if (distanceSq > 48 * 48) {
            return frame % 4 != 0;
        } else if (distanceSq > 32 * 32) {
            return frame % 2 != 0;
        }

        return false;
    }
}
