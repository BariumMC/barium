package com.barium.client.optimization;

import com.barium.config.BariumConfig;
import net.minecraft.client.particle.Particle;
import net.minecraft.util.math.Vec3d;

import java.util.concurrent.atomic.AtomicInteger;

public class ParticleOptimizer {

    private static final AtomicInteger particleCount = new AtomicInteger(0);

    // CORREÇÃO: Assinatura do método corrigida para aceitar a posição da câmera.
    public static boolean shouldSkipParticleTick(Particle particle, Vec3d cameraPos) {
        if (!BariumConfig.C.ENABLE_PARTICLE_OPTIMIZATION) return false;

        // CORREÇÃO: Acessando os campos públicos x, y, z em vez de métodos getX() que não existem.
        double distanceSq = cameraPos.squaredDistanceTo(particle.x, particle.y, particle.z);
        
        return distanceSq > BariumConfig.C.PARTICLE_CULL_DISTANCE_SQ;
    }
    
    public static boolean shouldCullNewParticle() {
        return particleCount.get() >= BariumConfig.C.MAX_GLOBAL_PARTICLES;
    }

    public static void incrementParticleCount() {
        particleCount.incrementAndGet();
    }
    
    public static void resetParticleCount() {
        particleCount.set(0);
    }
}