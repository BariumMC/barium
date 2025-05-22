package com.barium.client.optimization;

import com.barium.BariumMod;
import com.barium.config.BariumConfig;

import java.util.concurrent.atomic.AtomicInteger;

public class ParticleTracker {

    private static final AtomicInteger particleCount = new AtomicInteger(0);

    public static void incrementParticleCount() {
        if (!BariumConfig.ENABLE_MOD_OPTIMIZATIONS) return; // Não conta se otimizações desabilitadas
        particleCount.incrementAndGet();
        if (BariumConfig.ENABLE_DEBUG_LOGGING && particleCount.get() % 1000 == 0) {
            BariumMod.LOGGER.debug("Particle added. Current count: " + particleCount.get());
        }
    }

    public static void decrementParticleCount() {
        if (!BariumConfig.ENABLE_MOD_OPTIMIZATIONS) return; // Não conta se otimizações desabilitadas
        if (particleCount.get() > 0) {
            particleCount.decrementAndGet();
        }
        if (BariumConfig.ENABLE_DEBUG_LOGGING && particleCount.get() % 1000 == 0) {
            BariumMod.LOGGER.debug("Particle removed. Current count: " + particleCount.get());
        }
    }

    public static void resetParticleCount() {
        if (!BariumConfig.ENABLE_MOD_OPTIMIZATIONS) return;
        particleCount.set(0);
        BariumMod.LOGGER.info("Particle count reset.");
    }

    public static boolean isParticleLimitExceeded() {
        return BariumConfig.ENABLE_MOD_OPTIMIZATIONS && BariumConfig.ENABLE_PARTICLE_CULLING && // Culling deve estar ativo para o limite fazer sentido
               particleCount.get() >= BariumConfig.MAX_TOTAL_PARTICLES;
    }

    public static int getCurrentParticleCount() {
        return particleCount.get();
    }
}