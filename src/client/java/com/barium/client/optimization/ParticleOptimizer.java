package com.barium.client.optimization;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.render.Camera;

public class ParticleOptimizer {

    // Distância máxima ao quadrado para renderizar partículas (por ex., 64 blocos)
    private static final double MAX_RENDER_DISTANCE_SQUARED = 64 * 64;

    /**
     * Verifica se a partícula deve ser renderizada com base na distância até a câmera.
     *
     * @param particle A partícula a ser avaliada.
     * @param camera A câmera atual.
     * @return true se deve renderizar, false se pode ser ignorada.
     */
    public static boolean shouldRenderParticle(Particle particle, Camera camera) {
        double dx = particle.getX() - camera.getPos().x;
        double dy = particle.getY() - camera.getPos().y;
        double dz = particle.getZ() - camera.getPos().z;

        double distanceSquared = dx * dx + dy * dy + dz * dz;

        return distanceSquared <= MAX_RENDER_DISTANCE_SQUARED;
    }

    /**
     * Verifica se o tick da partícula pode ser pulado.
     * Aqui podemos usar a mesma regra de distância ou outra, se quiser.
     */
    public static boolean shouldSkipParticleTick(Particle particle, Camera camera) {
        // Reutilizamos a mesma regra de distância
        return !shouldRenderParticle(particle, camera);
    }
}
