package com.barium.client.optimization;

import com.barium.client.mixin.ParticleAccessor;
import com.barium.config.BariumConfig; // Importar BariumConfig
import net.minecraft.client.particle.Particle;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Frustum; // Importar Frustum
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public class ParticleOptimizer {

    // Removidas as constantes hardcoded, agora vêm de BariumConfig
    // private static final double MAX_RENDER_DISTANCE_SQ = 128 * 128;
    // private static final double MAX_TICK_DISTANCE_SQ = 128 * 128;

    // Inicialização do otimizador (pode ser chamada no BariumClient.java)
    public static void init() {
        // Se você implementar um sistema de configuração, carregue-o aqui.
        // BariumConfig.loadConfig(); // Exemplo
    }

    // Verifica se deve pular o tick (atualização da partícula)
    public static boolean shouldSkipParticleTick(Particle particle, Camera camera) {
        if (!BariumConfig.ENABLE_PARTICLE_OPTIMIZATION) return false; // Usa a nova flag

        ParticleAccessor accessor = (ParticleAccessor) particle;
        Vec3d particlePos = new Vec3d(accessor.getX(), accessor.getY(), accessor.getZ());
        Vec3d cameraPos = camera.getPos();

        double distanceSq = particlePos.squaredDistanceTo(cameraPos);
        return distanceSq > BariumConfig.MAX_TICK_DISTANCE_SQ; // Usa a variável de configuração
    }

    // Verifica se deve renderizar a partícula
    public static boolean shouldRenderParticle(Particle particle, Camera camera) {
        if (!BariumConfig.ENABLE_PARTICLE_OPTIMIZATION) return true; // Usa a nova flag

        ParticleAccessor accessor = (ParticleAccessor) particle;
        Vec3d particlePos = new Vec3d(accessor.getX(), accessor.getY(), accessor.getZ());
        Vec3d cameraPos = camera.getPos();

        double distanceSq = particlePos.squaredDistanceTo(cameraPos);
        if (distanceSq > BariumConfig.MAX_RENDER_DISTANCE_SQ) { // Usa a variável de configuração
            return false;
        }

        Box box = new Box(
            accessor.getX() - 0.1, accessor.getY() - 0.1, accessor.getZ() - 0.1,
            accessor.getX() + 0.1, accessor.getY() + 0.1, accessor.getZ() + 0.1
        );

        // Substituído isBoundingBoxInFrustum por getFrustum().isVisible()
        Frustum frustum = camera.getFrustum(); // Obtém a frustum da câmera
        return frustum.isVisible(box);
    }

    // Implementação de LOD (conceitual)
    // Este método seria chamado por um Mixin na renderização da partícula
    public static boolean shouldApplyLOD(Particle particle, Camera camera) {
        if (!BariumConfig.ENABLE_PARTICLE_LOD || !BariumConfig.ENABLE_PARTICLE_OPTIMIZATION) return false; // Usa as novas flags

        ParticleAccessor accessor = (ParticleAccessor) particle;
        Vec3d particlePos = new Vec3d(accessor.getX(), accessor.getY(), accessor.getZ());
        Vec3d cameraPos = camera.getPos();

        double distanceSq = particlePos.squaredDistanceTo(cameraPos);
        return distanceSq > BariumConfig.PARTICLE_LOD_DISTANCE * BariumConfig.PARTICLE_LOD_DISTANCE; // Usa a nova variável
    }
}