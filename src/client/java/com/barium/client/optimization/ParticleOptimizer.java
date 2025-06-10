package com.barium.client.optimization;

import com.barium.client.mixin.ParticleAccessor;
import com.barium.config.BariumConfig;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.render.Camera;
import net.minecraft.util.math.Vec3d;

public class ParticleOptimizer {

    public static void init() {
        // Nada a fazer aqui por enquanto
    }

    public static boolean shouldSkipParticleTick(Particle particle, Camera camera) {
        if (!BariumConfig.ENABLE_PARTICLE_OPTIMIZATION) return false;

        ParticleAccessor accessor = (ParticleAccessor) particle;
        Vec3d particlePos = new Vec3d(accessor.getX(), accessor.getY(), accessor.getZ());
        Vec3d cameraPos = camera.getPos();

        double distanceSq = particlePos.squaredDistanceTo(cameraPos);
        // CORREÇÃO: Usa a única flag de distância de partícula
        return distanceSq > BariumConfig.MAX_TICK_DISTANCE_SQ;
    }

    public static boolean shouldRenderParticle(Particle particle, Camera camera) {
        if (!BariumConfig.ENABLE_PARTICLE_OPTIMIZATION) return true;

        ParticleAccessor accessor = (ParticleAccessor) particle;
        Vec3d particlePos = new Vec3d(accessor.getX(), accessor.getY(), accessor.getZ());
        Vec3d cameraPos = camera.getPos();

        double distanceSq = particlePos.squaredDistanceTo(cameraPos);
        // CORREÇÃO: Usa a única flag de distância de partícula
        return distanceSq <= BariumConfig.MAX_TICK_DISTANCE_SQ;
    }
    
    // CORREÇÃO: A lógica de LOD foi removida pois era redundante e complexa.
}