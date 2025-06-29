package com.barium.client.optimization;

import com.barium.config.BariumConfig;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.render.Camera;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.concurrent.atomic.AtomicInteger;

public class ParticleOptimizer {

    private static final AtomicInteger particleCount = new AtomicInteger(0);

    public static boolean shouldSkipParticle(Particle particle, Camera camera) {
        if (!BariumConfig.C.ENABLE_PARTICLE_OPTIMIZATION) return false;

        // Verificação por distância primeiro, que é mais barata
        Vec3d particlePos = new Vec3d(particle.getX(), particle.getY(), particle.getZ());
        double distanceSq = particlePos.squaredDistanceTo(camera.getPos());
        // CORREÇÃO: Usando a variável de configuração que foi re-adicionada
        if (distanceSq > BariumConfig.C.PARTICLE_CULL_DISTANCE_SQ) {
            return true;
        }

        // Se estiver perto o suficiente, verifica o frustum
        Box box = particle.getBoundingBox();
        return !camera.getFrustum().isVisible(box);
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