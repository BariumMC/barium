package com.barium.optimization;

import com.barium.BariumMod;
import com.barium.config.BariumConfig;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.render.Camera;
import net.minecraft.util.math.Vec3d;

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
        if (!BariumConfig.ENABLE_PARTICLE_CULLING) {
            return true;
        }
        
        // Obtém a posição da partícula e da câmera
        Vec3d particlePos = getParticlePosition(particle);
        Vec3d cameraPos = camera.getPos();
        
        // Calcula a distância
        double distance = particlePos.distanceTo(cameraPos);
        
        // Culling baseado na distância
        if (distance > 128) {
            return false;
        }
        
        // Culling baseado no campo de visão
        if (isOutsideFieldOfView(particlePos, camera)) {
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
        if (!BariumConfig.ENABLE_PARTICLE_LOD) {
            return 0;
        }
        
        // Obtém a posição da partícula e da câmera
        Vec3d particlePos = getParticlePosition(particle);
        Vec3d cameraPos = camera.getPos();
        
        // Calcula a distância
        double distance = particlePos.distanceTo(cameraPos);
        
        // Determina o LOD com base na distância
        int lod = 0;
        if (distance > BariumConfig.PARTICLE_LOD_DISTANCE) {
            lod = (int) ((distance - BariumConfig.PARTICLE_LOD_DISTANCE) / 16);
            lod = Math.min(lod, 3); // Máximo de 3 níveis de LOD
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
        // Obtém o LOD desta partícula
        Integer lod = PARTICLE_LOD_LEVELS.get(particle);
        if (lod == null) {
            return true;
        }
        
        // Incrementa o contador de ticks para esta partícula
        int counter = PARTICLE_TICK_COUNTERS.getOrDefault(particle, 0) + 1;
        PARTICLE_TICK_COUNTERS.put(particle, counter);
        
        // Com base no LOD, reduz a frequência de atualizações
        switch (lod) {
            case 1:
                // Atualiza a cada 2 ticks
                return counter % 2 == 0;
            case 2:
                // Atualiza a cada 3 ticks
                return counter % 3 == 0;
            case 3:
                // Atualiza a cada 4 ticks
                return counter % 4 == 0;
            default:
                return true;
        }
    }
    
    /**
     * Obtém a posição de uma partícula
     * 
     * @param particle A partícula
     * @return A posição da partícula como Vec3d
     */
    private static Vec3d getParticlePosition(Particle particle) {
        // Usando reflection para acessar os campos protegidos
        // Em um mod real, seria melhor usar mixins para acessar esses campos
        try {
            java.lang.reflect.Field xField = Particle.class.getDeclaredField("x");
            java.lang.reflect.Field yField = Particle.class.getDeclaredField("y");
            java.lang.reflect.Field zField = Particle.class.getDeclaredField("z");
            
            xField.setAccessible(true);
            yField.setAccessible(true);
            zField.setAccessible(true);
            
            double x = xField.getDouble(particle);
            double y = yField.getDouble(particle);
            double z = zField.getDouble(particle);
            
            return new Vec3d(x, y, z);
        } catch (Exception e) {
            // Fallback para uma posição padrão em caso de erro
            BariumMod.LOGGER.error("Erro ao acessar posição da partícula: " + e.getMessage());
            return Vec3d.ZERO;
        }
    }
    
    /**
     * Verifica se uma posição está fora do campo de visão da câmera
     * 
     * @param position A posição a verificar
     * @param camera A câmera do jogador
     * @return true se a posição está fora do campo de visão, false caso contrário
     */
    private static boolean isOutsideFieldOfView(Vec3d position, Camera camera) {
        // Vetor da câmera para a posição
        Vec3d direction = position.subtract(camera.getPos()).normalize();
        
        // Obtém o vetor de direção da câmera
        Vec3d cameraDirection = Vec3d.fromPolar(camera.getPitch(), camera.getYaw());
        
        // Produto escalar com a direção da câmera
        double dot = direction.dotProduct(cameraDirection);
        
        // Se o produto escalar for negativo, a posição está atrás da câmera
        if (dot < 0) {
            return true;
        }
        
        // Verifica se está dentro do cone de visão (aproximadamente 90 graus)
        return dot < 0.5;
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
