package com.barium.client.optimization;

import com.barium.BariumMod;
import com.barium.config.BariumConfig;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Rastreador de partículas para otimizações e depuração.
 * Útil para limitar o número total de partículas.
 */
public class ParticleTracker {
    private static final AtomicInteger particleCount = new AtomicInteger(0);

    public static void addParticle() {
        if (!BariumConfig.ENABLE_MOD_OPTIMIZATIONS) return; // Don't count if optimizations are disabled
        particleCount.incrementAndGet();
        if (BariumConfig.ENABLE_DEBUG_LOGGING && particleCount.get() % 1000 == 0) {
            BariumMod.LOGGER.debug("Total particles: {}", particleCount.get());
        }
    }

    public static void removeParticle() {
        if (!BariumConfig.ENABLE_MOD_OPTIMIZATIONS) return; // Don't count if optimizations are disabled
        particleCount.decrementAndGet();
        if (BariumConfig.ENABLE_DEBUG_LOGGING && particleCount.get() % 1000 == 0) {
            BariumMod.LOGGER.debug("Total particles: {}", particleCount.get());
        }
    }

    public static int getParticleCount() {
        return particleCount.get();
    }

    /**
     * Verifica se o limite total de partículas foi atingido.
     * @return true se o limite foi atingido, false caso contrário.
     */
    public static boolean isParticleLimitReached() {
        return BariumConfig.ENABLE_MOD_OPTIMIZATIONS && BariumConfig.ENABLE_PARTICLE_CULLING && // Culling must be active for the limit to make sense
               particleCount.get() >= BariumConfig.MAX_TOTAL_PARTICLES;
    }

    public static void clear() {
        particleCount.set(0);
        BariumMod.LOGGER.info("ParticleTracker cleared.");
    }
}