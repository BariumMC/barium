package com.barium.client.optimization;

import com.barium.BariumMod;
import com.barium.config.BariumConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.render.Camera;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

/**
 * Otimiza o sistema de partículas, aplicando culling e LOD.
 * Baseado nos mappings Yarn 1.21.5+build.1
 * Corrigido: Adicionado método init() para inicialização.
 * Corrigido: Uso correto de frustum.isVisible(Box).
 */
public class ParticleOptimizer {

    // Distância quadrada máxima para renderizar partículas com detalhes completos
    private static final double MAX_DETAIL_DISTANCE_SQ = 32 * 32; // 32 blocos
    // Distância quadrada máxima para renderizar partículas (LOD)
    private static final double MAX_RENDER_DISTANCE_SQ = 64 * 64; // 64 blocos

    /**
     * Inicializa o otimizador de partículas.
     */
    public static void init() {
        BariumMod.LOGGER.info("Inicializando ParticleOptimizer");
    }

    /**
     * Verifica se uma partícula deve ser renderizada com base na distância e visibilidade.
     *
     * @param particle A partícula.
     * @param camera A câmera do jogo.
     * @param world O mundo do cliente (world da partícula, passado pelo mixin).
     * @return true se a partícula deve ser renderizada.
     */
    public static boolean shouldRenderParticle(Particle particle, Camera camera, ClientWorld world) {
        if (!BariumConfig.ENABLE_PARTICLE_OPTIMIZATION) {
            return true; // Renderiza normalmente se a otimização estiver desativada
        }

        Vec3d cameraPos = camera.getPos();
        
        Box boundingBox = particle.getBoundingBox();
        if (boundingBox == null) {
            return true; // Se não tem bounding box, renderiza por segurança
        }
        
        Vec3d particlePos = boundingBox.getCenter();

        double distanceSq = cameraPos.squaredDistanceTo(particlePos);

        // Culling por distância máxima
        if (distanceSq > MAX_RENDER_DISTANCE_SQ) {
            return false;
        }

        // Culling por frustum
        // O método getFrustum() existe na classe Camera em Yarn 1.21.5
        if (!camera.getFrustum().isVisible(boundingBox)) {
             return false;
        }

        // Aplica LOD (Level of Detail) - Simplifica ou pula o tick se estiver longe
        if (BariumConfig.ENABLE_PARTICLE_LOD && distanceSq > MAX_DETAIL_DISTANCE_SQ) {
            // A lógica de pular o tick para LOD já é tratada em shouldSkipParticleTick.
            // Para renderização, podemos decidir se queremos um modelo simplificado ou frame-skip visual aqui.
            // Por enquanto, apenas a parte do culling por distância/frustum é aplicada na renderização.
        }

        return true; // Renderiza a partícula
    }

    /**
     * Verifica se o tick de uma partícula deve ser pulado com base na distância (LOD).
     *
     * @param particle A partícula.
     * @param world O mundo.
     * @return true se o tick deve ser pulado.
     */
    public static boolean shouldSkipParticleTick(Particle particle, ClientWorld world) {
        if (!BariumConfig.ENABLE_PARTICLE_OPTIMIZATION || !BariumConfig.ENABLE_PARTICLE_LOD) {
            return false;
        }

        Camera camera = MinecraftClient.getInstance().gameRenderer.getCamera();
        Vec3d cameraPos = camera.getPos();
        
        Box boundingBox = particle.getBoundingBox();
        if (boundingBox == null) {
            return false;
        }
        
        Vec3d particlePos = boundingBox.getCenter();
        
        double distanceSq = cameraPos.squaredDistanceTo(particlePos);

        if (distanceSq > MAX_DETAIL_DISTANCE_SQ) {
            // Pula o tick em frames alternados para partículas distantes
            return (world.getTime() + particle.hashCode()) % 2 != 0;
        }

        return false;
    }
}