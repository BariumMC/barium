package com.barium.client.optimization;

import com.barium.client.mixin.ParticleAccessor;
import com.barium.config.BariumConfig; // Importar a nova BariumConfig
import net.minecraft.client.particle.Particle;
import net.minecraft.client.render.Camera;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public class ParticleOptimizer {

    // Remova estas constantes, elas virão da BariumConfig
    // private static final double MAX_RENDER_DISTANCE_SQ = 128 * 128;
    // private static final double MAX_TICK_DISTANCE_SQ = 128 * 128;

    // Inicialização do otimizador (pode ser chamada no BariumClient.java)
    public static void init() {
        // Se você implementar um sistema de configuração, carregue-o aqui.
        // BariumConfig.loadConfig(); // Exemplo
    }

    // Verifica se deve pular o tick (atualização da partícula)
    public static boolean shouldSkipParticleTick(Particle particle, Camera camera) {
        if (!BariumConfig.enableParticleOptimization) return false; // Se a otimização de partículas estiver desativada

        ParticleAccessor accessor = (ParticleAccessor) particle;
        Vec3d particlePos = new Vec3d(accessor.getX(), accessor.getY(), accessor.getZ());
        Vec3d cameraPos = camera.getPos();

        double distanceSq = particlePos.squaredDistanceTo(cameraPos);
        return distanceSq > BariumConfig.maxTickDistanceSq; // Use o valor da configuração
    }

    // Verifica se deve renderizar a partícula
    public static boolean shouldRenderParticle(Particle particle, Camera camera) {
        if (!BariumConfig.enableParticleOptimization) return true; // Se a otimização de partículas estiver desativada

        ParticleAccessor accessor = (ParticleAccessor) particle;
        Vec3d particlePos = new Vec3d(accessor.getX(), accessor.getY(), accessor.getZ());
        Vec3d cameraPos = camera.getPos();

        double distanceSq = particlePos.squaredDistanceTo(cameraPos);
        if (distanceSq > BariumConfig.maxRenderDistanceSq) { // Use o valor da configuração
            return false;
        }

        Box box = new Box(
            accessor.getX() - 0.1, accessor.getY() - 0.1, accessor.getZ() - 0.1,
            accessor.getX() + 0.1, accessor.getY() + 0.1, accessor.getZ() + 0.1
        );

        // A linha original já estava usando isBoundingBoxInFrustum, o que é o correto para versões recentes
        return camera.isBoundingBoxInFrustum(box);
    }

    // Implementação de LOD (conceitual)
    // Este método seria chamado por um Mixin na renderização da partícula
    public static boolean shouldApplyLOD(Particle particle, Camera camera) {
        if (!BariumConfig.enableParticleLOD || !BariumConfig.enableParticleOptimization) return false; //

        ParticleAccessor accessor = (ParticleAccessor) particle;
        Vec3d particlePos = new Vec3d(accessor.getX(), accessor.getY(), accessor.getZ());
        Vec3d cameraPos = camera.getPos();

        double distanceSq = particlePos.squaredDistanceTo(cameraPos);
        return distanceSq > BariumConfig.particleLODDistance * BariumConfig.particleLODDistance;
    }
}