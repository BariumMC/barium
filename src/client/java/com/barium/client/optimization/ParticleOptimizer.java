package com.barium.client.optimization;

import com.barium.BariumMod;
import com.barium.config.BariumConfig;
import com.barium.mixin.particle.ParticleAccessor;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.render.Camera;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.Map;
import java.util.WeakHashMap;

public class ParticleOptimizer {
    private static final Map<Particle, Integer> PARTICLE_LOD_LEVELS = new WeakHashMap<>();
    private static final Map<Particle, Integer> PARTICLE_TICK_COUNTERS = new WeakHashMap<>();

    public static void init() {
        BariumMod.LOGGER.info("Inicializando otimizações do sistema de partículas e efeitos");
        BariumConfig.printConfig();
    }

    public static int getParticleLOD(Particle particle, Camera camera) {
        if (!BariumConfig.ENABLE_PARTICLE_LOD) {
            PARTICLE_LOD_LEVELS.put(particle, 0);
            return 0;
        }

        Vec3d particlePos = getParticlePosition(particle);
        Vec3d cameraPos = camera.getPos();

        double dx = particlePos.x - cameraPos.x;
        double dy = particlePos.y - cameraPos.y;
        double dz = particlePos.z - cameraPos.z;
        double distanceSq = dx * dx + dy * dy + dz * dz;

        int lod = 0;
        if (distanceSq > BariumConfig.PARTICLE_LOD_DISTANCE_START * BariumConfig.PARTICLE_LOD_DISTANCE_START) {
            double distance = Math.sqrt(distanceSq);
            lod = (int) ((distance - BariumConfig.PARTICLE_LOD_DISTANCE_START) / BariumConfig.PARTICLE_LOD_STEP_DISTANCE);
            lod = Math.min(lod, BariumConfig.MAX_PARTICLE_LOD_LEVELS);
            lod = Math.max(lod, 0);
        }

        PARTICLE_LOD_LEVELS.put(particle, lod);
        return lod;
    }

    public static boolean shouldTickParticle(Particle particle, Camera camera) {
        if (BariumConfig.ENABLE_DISTANCE_CULLING) {
            Vec3d particlePos = getParticlePosition(particle);
            Vec3d cameraPos = camera.getPos();

            double dx = particlePos.x - cameraPos.x;
            double dy = particlePos.y - cameraPos.y;
            double dz = particlePos.z - cameraPos.z;
            double distanceSq = dx * dx + dy * dy + dz * dz;

            if (distanceSq > BariumConfig.CULLING_DISTANCE_SQ) {
                if (BariumConfig.ENABLE_DEBUG_LOGGING) {
                    BariumMod.LOGGER.debug("Culling (tick): Particle too far (" + Math.sqrt(distanceSq) + " blocks). Expiring.");
                }
                return false;
            }
        }

        if (BariumConfig.ENABLE_PARTICLE_LOD) {
            int lod = getParticleLOD(particle, camera);
            if (lod > 0) {
                int counter = PARTICLE_TICK_COUNTERS.getOrDefault(particle, 0) + 1;
                PARTICLE_TICK_COUNTERS.put(particle, counter);

                if (counter % (lod + 1) != 0) {
                    if (BariumConfig.ENABLE_DEBUG_LOGGING) {
                        BariumMod.LOGGER.debug("Culling (tick): Particle at LOD " + lod + " skipped tick.");
                    }
                    return false;
                }
            }
        }
        return true;
    }

    public static Vec3d getParticlePosition(Particle particle) {
        ParticleAccessor accessor = (ParticleAccessor) particle;
        return new Vec3d(accessor.barium$getX(), accessor.barium$getY(), accessor.barium$getZ());
    }

    // shouldRenderParticle permanece a mesma, pois é a lógica que decide se a partícula é visível
    public static boolean shouldRenderParticle(Particle particle, Camera camera) {
        if (BariumConfig.ENABLE_DISTANCE_CULLING) {
            Vec3d particlePos = getParticlePosition(particle);
            Vec3d cameraPos = camera.getPos();

            double dx = particlePos.x - cameraPos.x;
            double dy = particlePos.y - cameraPos.y;
            double dz = particlePos.z - cameraPos.z;
            double distanceSq = dx * dx + dy * dy + dz * dz;

            if (distanceSq > BariumConfig.CULLING_DISTANCE_SQ) {
                if (BariumConfig.ENABLE_DEBUG_LOGGING) {
                    BariumMod.LOGGER.debug("Culling (render): Particle too far (" + Math.sqrt(distanceSq) + " blocks). Skipping render.");
                }
                return false;
            }
        }

        if (BariumConfig.ENABLE_FRUSTUM_CULLING) {
            Box particleBox = particle.getBoundingBox();
            if (!camera.isBoxVisible(particleBox)) {
                if (BariumConfig.ENABLE_DEBUG_LOGGING) {
                    BariumMod.LOGGER.debug("Culling (render): Particle outside frustum. Skipping render.");
                }
                return false;
            }
        }
        return true;
    }

    public static void removeParticle(Particle particle) {
        PARTICLE_LOD_LEVELS.remove(particle);
        PARTICLE_TICK_COUNTERS.remove(particle);
        if (BariumConfig.ENABLE_DEBUG_LOGGING) {
            BariumMod.LOGGER.debug("Removed particle from optimizer maps.");
        }
    }
}