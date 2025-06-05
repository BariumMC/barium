package com.barium.client.optimization;

import com.barium.client.mixin.ParticleAccessor;
import com.barium.config.BariumConfig;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.render.Camera;
// import net.minecraft.client.render.Frustum; // Removido, pois getFrustum() não está disponível
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public class ParticleOptimizer {

    // Inicialização do otimizador (pode ser chamada no BariumClient.java)
    public static void init() {
        // Se você implementar um sistema de configuração, carregue-o aqui.
        // BariumConfig.loadConfig(); // Exemplo
    }

    // Verifica se deve pular o tick (atualização da partícula)
    public static boolean shouldSkipParticleTick(Particle particle, Camera camera) {
        if (!BariumConfig.ENABLE_PARTICLE_OPTIMIZATION) return false;

        ParticleAccessor accessor = (ParticleAccessor) particle;
        Vec3d particlePos = new Vec3d(accessor.getX(), accessor.getY(), accessor.getZ());
        Vec3d cameraPos = camera.getPos();

        double distanceSq = particlePos.squaredDistanceTo(cameraPos);
        return distanceSq > BariumConfig.MAX_TICK_DISTANCE_SQ;
    }

    // Verifica se deve renderizar a partícula
    public static boolean shouldRenderParticle(Particle particle, Camera camera) {
        if (!BariumConfig.ENABLE_PARTICLE_OPTIMIZATION) return true;

        ParticleAccessor accessor = (ParticleAccessor) particle;
        Vec3d particlePos = new Vec3d(accessor.getX(), accessor.getY(), accessor.getZ());
        Vec3d cameraPos = camera.getPos();

        double distanceSq = particlePos.squaredDistanceTo(cameraPos);
        if (distanceSq > BariumConfig.MAX_RENDER_DISTANCE_SQ) {
            return false;
        }

        // A verificação de frustum culling foi removida temporariamente
        // porque nem camera.isBoundingBoxInFrustum() nem camera.getFrustum().isVisible()
        // parecem estar disponíveis ou funcionando conforme esperado para a versão 1.21.5
        // com os mappings atuais.
        // Para uma otimização mais robusta aqui, seria necessário investigar os mappings
        // específicos da Camera para 1.21.5 ou usar uma abordagem diferente para frustum culling.
        
        return true; // Retorna sempre true se passar pela checagem de distância
    }

    // Implementação de LOD (conceitual)
    public static boolean shouldApplyLOD(Particle particle, Camera camera) {
        if (!BariumConfig.ENABLE_PARTICLE_LOD || !BariumConfig.ENABLE_PARTICLE_OPTIMIZATION) return false;

        ParticleAccessor accessor = (ParticleAccessor) particle;
        Vec3d particlePos = new Vec3d(accessor.getX(), accessor.getY(), accessor.getZ());
        Vec3d cameraPos = camera.getPos();

        double distanceSq = particlePos.squaredDistanceTo(cameraPos);
        return distanceSq > BariumConfig.PARTICLE_LOD_DISTANCE * BariumConfig.PARTICLE_LOD_DISTANCE;
    }
}