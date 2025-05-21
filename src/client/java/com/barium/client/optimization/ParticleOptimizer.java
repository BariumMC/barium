package com.barium.client.optimization;

import com.barium.BariumMod;
import com.barium.config.BariumConfig;
import com.barium.mixin.client.particle.ParticleAccessor; // Importa a interface Accessor
import net.minecraft.client.particle.Particle;
import net.minecraft.client.render.Camera;
import net.minecraft.util.math.Vec3d;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * Otimizador do sistema de partículas e efeitos.
 * Contém a lógica central de culling e LOD para partículas.
 */
public class ParticleOptimizer {
    // Mapa para controlar o nível de detalhe das partículas
    // WeakHashMap é usado para que as entradas sejam removidas automaticamente
    // quando a partícula não é mais referenciada (e, portanto, coletada pelo GC).
    private static final Map<Particle, Integer> PARTICLE_LOD_LEVELS = new WeakHashMap<>();
    
    // Mapa para armazenar contadores de ticks para partículas (para LOD de ticking)
    private static final Map<Particle, Integer> PARTICLE_TICK_COUNTERS = new WeakHashMap<>();
    
    public static void init() {
        BariumMod.LOGGER.info("Inicializando otimizações do sistema de partículas e efeitos");
    }
    
    /**
     * Determina o nível de detalhe (LOD) para uma partícula com base na distância.
     * Este método também armazena o LOD calculado no mapa.
     * 
     * @param particle A partícula.
     * @param camera A câmera do jogador.
     * @return O nível de detalhe (0 = máximo, maior = menos detalhes).
     */
    public static int getParticleLOD(Particle particle, Camera camera) {
        if (!BariumConfig.ENABLE_PARTICLE_LOD) {
            PARTICLE_LOD_LEVELS.put(particle, 0); // Garante que partículas tenham LOD 0 se a função estiver desabilitada
            return 0;
        }
        
        // Obtém a posição da partícula usando o Accessor Mixin
        Vec3d particlePos = getParticlePosition(particle);
        Vec3d cameraPos = camera.getPos();
        
        // Calcula a distância
        double distance = particlePos.distanceTo(cameraPos);
        
        // Determina o LOD com base na distância
        int lod = 0;
        if (distance > BariumConfig.PARTICLE_LOD_DISTANCE_START) {
            lod = (int) ((distance - BariumConfig.PARTICLE_LOD_DISTANCE_START) / BariumConfig.PARTICLE_LOD_STEP_DISTANCE);
            lod = Math.min(lod, BariumConfig.MAX_PARTICLE_LOD_LEVELS); // Limita o LOD máximo
        }
        
        // Armazena o LOD para esta partícula
        PARTICLE_LOD_LEVELS.put(particle, lod);
        
        return lod;
    }
    
    /**
     * Verifica se uma partícula deve ser atualizada (ticked) neste tick,
     * com base no seu nível de detalhe (LOD).
     * 
     * @param particle A partícula.
     * @return true se a partícula deve ser atualizada, false caso contrário.
     */
    public static boolean shouldTickParticle(Particle particle) {
        if (!BariumConfig.ENABLE_PARTICLE_LOD) {
            return true; // Se o LOD estiver desabilitado, sempre faz o tick
        }

        // Obtém o LOD desta partícula. Se não houver, assume LOD 0 (máximo detalhe).
        Integer lod = PARTICLE_LOD_LEVELS.getOrDefault(particle, 0);
        if (lod == 0) {
            return true; // Partículas com LOD 0 sempre são atualizadas
        }
        
        // Incrementa o contador de ticks para esta partícula
        int counter = PARTICLE_TICK_COUNTERS.getOrDefault(particle, 0) + 1;
        PARTICLE_TICK_COUNTERS.put(particle, counter);
        
        // Com base no LOD, reduz a frequência de atualizações
        // LOD 1: atualiza a cada 2 ticks
        // LOD 2: atualiza a cada 3 ticks
        // LOD 3: atualiza a cada 4 ticks
        // E assim por diante, para lod > 0, atualiza a cada (lod + 1) ticks
        return counter % (lod + 1) == 0;
    }
    
    /**
     * Obtém a posição de uma partícula usando a interface Accessor Mixin.
     * 
     * @param particle A partícula.
     * @return A posição da partícula como Vec3d.
     */
    private static Vec3d getParticlePosition(Particle particle) {
        ParticleAccessor accessor = (ParticleAccessor) particle;
        return new Vec3d(accessor.barium$getX(), accessor.barium$getY(), accessor.barium$getZ());
    }
    
    /**
     * Verifica se uma posição está fora do campo de visão da câmera.
     * 
     * @param position A posição a verificar.
     * @param camera A câmera do jogador.
     * @return true se a posição está fora do campo de visão, false caso contrário.
     */
    public static boolean isOutsideFieldOfView(Vec3d position, Camera camera) {
        // Vetor da câmera para a posição
        Vec3d direction = position.subtract(camera.getPos()).normalize();
        
        // Obtém o vetor de direção da câmera
        Vec3d cameraDirection = Vec3d.fromPolar(camera.getPitch(), camera.getYaw());
        
        // Produto escalar com a direção da câmera
        double dot = direction.dotProduct(cameraDirection);
        
        // Se o produto escalar for menor que o limiar, a posição está fora do FOV (ou atrás da câmera)
        // Um valor de 0.5 corresponde a um ângulo de aproximadamente 60 graus (cos(60)).
        // Ajuste BariumConfig.PARTICLE_CULLING_FOV_THRESHOLD para controlar a largura do FOV.
        return dot < BariumConfig.PARTICLE_CULLING_FOV_THRESHOLD;
    }
    
    /**
     * Remove uma partícula do sistema de LOD e tick para liberar memória.
     * 
     * @param particle A partícula a remover.
     */
    public static void removeParticle(Particle particle) {
        PARTICLE_LOD_LEVELS.remove(particle);
        PARTICLE_TICK_COUNTERS.remove(particle);
    }
}