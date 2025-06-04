package com.barium.client.optimization;

import com.barium.client.mixin.ParticleAccessor;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.render.Camera;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public class ParticleOptimizer {

    private static final double MAX_RENDER_DISTANCE_SQ = 128 * 128;
    private static final double MAX_TICK_DISTANCE_SQ = 128 * 128;

    // Verifica se deve pular o tick (atualização da partícula)
    public static boolean shouldSkipParticleTick(Particle particle, Camera camera) {
        ParticleAccessor accessor = (ParticleAccessor) particle;
        Vec3d particlePos = new Vec3d(accessor.getX(), accessor.getY(), accessor.getZ());
        Vec3d cameraPos = camera.getPos();

        double distanceSq = particlePos.squaredDistanceTo(cameraPos);
        return distanceSq > MAX_TICK_DISTANCE_SQ;
    }

    // Verifica se deve renderizar a partícula
    public static boolean shouldRenderParticle(Particle particle, Camera camera) {
        ParticleAccessor accessor = (ParticleAccessor) particle;
        Vec3d particlePos = new Vec3d(accessor.getX(), accessor.getY(), accessor.getZ());
        Vec3d cameraPos = camera.getPos();

        double distanceSq = particlePos.squaredDistanceTo(cameraPos);
        if (distanceSq > MAX_RENDER_DISTANCE_SQ) {
            return false;
        }

        Box box = new Box(
            accessor.getX() - 0.1, accessor.getY() - 0.1, accessor.getZ() - 0.1,
            accessor.getX() + 0.1, accessor.getY() + 0.1, accessor.getZ() + 0.1
        );

        // Aqui o método getFrustum() pode estar disponível — caso não esteja, veja abaixo (comentado)
    //    return camera.getFrustum().isVisible(box);

        // Se getFrustum() não existir na sua versão de Camera, remova essa linha acima e descomente abaixo:
        return camera.isBoundingBoxInFrustum(box);
    }
}
