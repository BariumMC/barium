package com.barium.client.optimization;

import com.barium.client.mixin.ParticleAccessor;
import com.barium.config.BariumConfig;
import net.minecraft.client.particle.Particle;
import net.minecraft.util.math.Vec3d;

import java.util.concurrent.atomic.AtomicInteger;

public class ParticleOptimizer {

    private static final AtomicInteger particleCount = new AtomicInteger(0);
    private static int maxParticles = 4096;

    public static boolean shouldSkipParticleTick(Particle particle, Vec3d cameraPos) {
        if (!BariumConfig.C.ENABLE_PARTICLE_OPTIMIZATION) return false;

        // CORREÇÃO: Usando a interface ParticleAccessor para acessar as coordenadas protegidas.
        ParticleAccessor accessor = (ParticleAccessor) particle;
        double distanceSq = cameraPos.squaredDistanceTo(accessor.getX(), accessor.getY(), accessor.getZ());
        
        return distanceSq > BariumConfig.C.PARTICLE_CULL_DISTANCE_SQ;
    }
    
    public static boolean shouldCullNewParticle() {
        return particleCount.get() >= maxParticles;
    }

    public static void incrementParticleCount() {
        particleCount.incrementAndGet();
    }
    
    public static void setMaxParticles(int max) {
        maxParticles = max;
    }
}