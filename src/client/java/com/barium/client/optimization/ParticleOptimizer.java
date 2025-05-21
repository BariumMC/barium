package com.barium.client.optimization;

import com.barium.BariumMod;
import com.barium.config.BariumConfig;
import com.barium.client.mixin.particle.ParticleAccessor;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.render.Camera;
import net.minecraft.util.math.Vec3d;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * Otimizador do sistema de partículas e efeitos.
 * 
 * Implementa:
 * - Culling de partículas fora do campo de visão
 * - LOD (Level of Detail) para partículas a distância
 */
public class ParticleOptimizer {
    // Mapa para controlar o nível de detalhe das partículas
    private static final Map<Particle, Integer> PARTICLE_LOD_LEVELS = new WeakHashMap<>();
    
    // Mapa para armazenar contadores de ticks para partículas
    private static final Map<Particle, Integer> PARTICLE_TICK_COUNTERS = new WeakHashMap<>();
    
    public static void init() {
        BariumMod.LOGGER.info("Inicializando otimizações do sistema de partículas e efeitos");
    }
    
    /**
     * Determina o nível de detalhe para uma partícula
     * 
     * @param particle A partícula
     * @param camera A câmera do jogador
     * @return O nível de detalhe (0 = máximo, maior = menos detalhes)
     */
    public static int getParticleLOD(Particle particle, Camera camera) {
        if (!BariumConfig.ENABLE_PARTICLE_LOD) {
            PARTICLE_LOD_LEVELS.put(particle, 0); // Garante que partículas tenham LOD 0 se a função estiver desabilitada
            return 0;
        }
        
        Vec3d particlePos = getParticlePosition(particle);
        Vec3d cameraPos = camera.getPos();
        
        double distance = particlePos.distanceTo(cameraPos);
        
        int lod = 0;
        if (distance > BariumConfig.PARTICLE_LOD_DISTANCE_START) {
            lod = (int) ((distance - BariumConfig.PARTICLE_LOD_DISTANCE_START) / BariumConfig.PARTICLE_LOD_STEP_DISTANCE);
            lod = Math.min(lod, BariumConfig.MAX_PARTICLE_LOD_LEVELS);
        }
        
        PARTICLE_LOD_LEVELS.put(particle, lod);
        
        return lod;
    }
    
    /**
     * Verifica se uma partícula deve ser atualizada neste tick
     * 
     * @param particle A partícula
     * @return true se a partícula deve ser atualizada, false caso contrário
     */
    public static boolean shouldTickParticle(Particle particle) {
        if (!BariumConfig.ENABLE_PARTICLE_LOD) {
            return true;
        }

        Integer lod = PARTICLE_LOD_LEVELS.getOrDefault(particle, 0);
        if (lod == 0) {
            return true;
        }
        
        int counter = PARTICLE_TICK_COUNTERS.getOrDefault(particle, 0) + 1;
        PARTICLE_TICK_COUNTERS.put(particle, counter);
        
        return counter % (lod + 1) == 0;
    }
    
    /**
     * Obtém a posição de uma partícula.
     * Mudado para PUBLIC para ser acessível pelos Mixins (ParticleManagerMixin).
     * 
     * @param particle A partícula
     * @return A posição da partícula como Vec3d
     */
    public static Vec3d getParticlePosition(Particle particle) { // AGORA PUBLIC
        ParticleAccessor accessor = (ParticleAccessor) particle;
        return new Vec3d(accessor.barium$getX(), accessor.barium$getY(), accessor.barium$getZ());
    }
    
    /**
     * Verifica se uma posição está fora do campo de visão da câmera
     * 
     * @param position A posição a verificar
     * @param camera A câmera do jogador
     * @return true se a posição está fora do campo de visão, false caso contrário
     */
    public static boolean isOutsideFieldOfView(Vec3d position, Camera camera) {
        Vec3d direction = position.subtract(camera.getPos()).normalize();
        Vec3d cameraDirection = Vec3d.fromPolar(camera.getPitch(), camera.getYaw());
        double dot = direction.dotProduct(cameraDirection);
        return dot < BariumConfig.PARTICLE_CULLING_FOV_THRESHOLD;
    }
    
    /**
     * Remove uma partícula do sistema de LOD
     * 
     * @param particle A partícula a remover
     */
    public static void removeParticle(Particle particle) {
        PARTICLE_LOD_LEVELS.remove(particle);
        PARTICLE_TICK_COUNTERS.remove(particle);
    }
}