package com.barium.client.optimization;

import com.barium.BariumMod;
import com.barium.config.BariumConfig;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.render.Camera;
import net.minecraft.util.math.Box; // <<--- MAKE SURE THIS IMPORT IS PRESENT AND CORRECT
import net.minecraft.util.math.Vec3d;
import com.barium.client.mixin.accessor.ParticleAccessor;

import java.util.HashMap;
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
     * Verifica se uma partícula deve ser renderizada
     * 
     * @param particle A partícula
     * @param camera A câmera do jogador
     * @return true se a partícula deve ser renderizada, false caso contrário
     */
    public static boolean shouldRenderParticle(Particle particle, Camera camera) {
        if (!BariumConfig.ENABLE_MOD_OPTIMIZATIONS || !BariumConfig.ENABLE_PARTICLE_CULLING) {
            return true;
        }
        
        // Obtém a posição da partícula e da câmera
        Vec3d particlePos = getParticlePosition(particle);
        Vec3d cameraPos = camera.getPos();
        
        // Calcula a distância quadrada para evitar sqrt
        double distanceSq = particlePos.squaredDistanceTo(cameraPos);
        
        // Culling baseado na distância
        if (distanceSq > BariumConfig.PARTICLE_CULLING_DISTANCE_SQ) {
            if (BariumConfig.ENABLE_DEBUG_LOGGING) {
                BariumMod.LOGGER.debug("Culling particle by distance: {}", distanceSq);
            }
            return false;
        }
        
        // Culling baseado no campo de visão (frustum culling)
        // This is the line causing the error. Verify 'Box' import and Camera.isFrustumVisible(Box) exists.
        // If it still fails, you might have to revert to an older method name or implement manually.
        if (!camera.isFrustumVisible(particle.getBoundingBox())) {
             if (BariumConfig.ENABLE_DEBUG_LOGGING) {
                BariumMod.LOGGER.debug("Culling particle by frustum: {}", particlePos);
            }
            return false;
        }
        
        return true;
    }
    
    /**
     * Determina o nível de detalhe para uma partícula
     * 
     * @param particle A partícula
     * @param camera A câmera do jogador
     * @return O nível de detalhe (0 = máximo, maior = menos detalhes)
     */
    public static int getParticleLOD(Particle particle, Camera camera) {
        if (!BariumConfig.ENABLE_MOD_OPTIMIZATIONS || !BariumConfig.ENABLE_PARTICLE_LOD) {
            return 0;
        }
        
        // Obtém a posição da partícula e da câmera
        Vec3d particlePos = getParticlePosition(particle);
        Vec3d cameraPos = camera.getPos();
        
        // Calcula a distância
        double distance = particlePos.distanceTo(cameraPos);
        
        // Determina o LOD com base na distância
        int lod = 0;
        if (distance > BariumConfig.PARTICLE_LOD_DISTANCE_START) {
            lod = (int) ((distance - BariumConfig.PARTICLE_LOD_DISTANCE_START) / BariumConfig.PARTICLE_LOD_STEP_DISTANCE);
            lod = Math.min(lod, BariumConfig.MAX_PARTICLE_LOD_LEVELS); // Máximo de N níveis de LOD
        }
        
        // Armazena o LOD para esta partícula
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
        if (!BariumConfig.ENABLE_MOD_OPTIMIZATIONS) {
            return true;
        }

        // Obtém o LOD desta partícula
        Integer lod = PARTICLE_LOD_LEVELS.get(particle);
        if (lod == null) {
            // Se LOD não foi determinado ainda (ex: partícula recém-criada), ticka normalmente
            return true;
        }
        
        // Incrementa o contador de ticks para esta partícula
        int counter = PARTICLE_TICK_COUNTERS.getOrDefault(particle, 0) + 1;
        PARTICLE_TICK_COUNTERS.put(particle, counter);
        
        // Com base no LOD, reduz a frequência de atualizações
        switch (lod) {
            case 0:
                return true; // Full tick
            case 1:
                return counter % 2 == 0; // Tick every 2nd frame
            case 2:
                return counter % 3 == 0; // Tick every 3rd frame
            case 3:
                return counter % 4 == 0; // Tick every 4th frame
            default:
                return counter % 5 == 0; // For higher LOD levels, tick even less frequently
        }
    }
    
    /**
     * Obtém a posição de uma partícula
     * 
     * @param particle A partícula
     * @return A posição da partícula como Vec3d
     */
    private static Vec3d getParticlePosition(Particle particle) {
        // Use the accessor to get protected fields
        ParticleAccessor accessor = (ParticleAccessor) particle;
        return new Vec3d(accessor.getX(), accessor.getY(), accessor.getZ());
    }
    
    /**
     * Verifica se uma posição está fora do campo de visão da câmera
     * (This method is no longer used directly as `camera.isFrustumVisible` handles it)
     * 
     * @param position A posição a verificar
     * @param camera A câmera do jogador
     * @return true se a posição está fora do campo de visão, false caso contrário
     */
    private static boolean isOutsideFieldOfView(Vec3d position, Camera camera) {
        // This method is kept for reference but not used by shouldRenderParticle
        // as `camera.isFrustumVisible` is more accurate for frustum culling.

        Vec3d direction = position.subtract(camera.getPos()).normalize();
        
        Vec3d cameraDirection = Vec3d.fromPolar(camera.getPitch(), camera.getYaw());
        
        double dot = direction.dotProduct(cameraDirection);
        
        if (dot < 0) {
            return true;
        }
        
        return dot < 0.5;
    }
    
    /**
     * Remove uma partícula do sistema de LOD
     * 
     * @param particle A partícula a remover
     */
    public static void removeParticle(Particle particle) {
        if (BariumConfig.ENABLE_DEBUG_LOGGING) {
            // Particles do not have UUIDs. Log a more generic identifier.
            BariumMod.LOGGER.debug("Removing particle from optimizers: {}", particle.getClass().getSimpleName());
        }
        PARTICLE_LOD_LEVELS.remove(particle);
        PARTICLE_TICK_COUNTERS.remove(particle);
    }
}