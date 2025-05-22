package com.barium.client.optimization;

import com.barium.BariumMod;
import com.barium.config.BariumConfig;
import com.barium.mixin.particle.ParticleAccessor;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.render.Camera;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * Otimizador do sistema de partículas e efeitos.
 *
 * Implementa:
 * - Culling de partículas fora do campo de visão (frustum culling)
 * - Culling de partículas muito distantes
 * - LOD (Level of Detail) para frequência de tick
 */
public class ParticleOptimizer {
    private static final Map<Particle, Integer> PARTICLE_LOD_LEVELS = new WeakHashMap<>();
    private static final Map<Particle, Integer> PARTICLE_TICK_COUNTERS = new WeakHashMap<>();

    public static void init() {
        BariumMod.LOGGER.info("Inicializando otimizações do sistema de partículas e efeitos");
        // BariumConfig.printConfig() é chamado no BariumMod.java
    }

    /**
     * Determina o nível de detalhe para uma partícula baseado na distância.
     * O nível de detalhe afeta a frequência de tick.
     *
     * @param particle A partícula
     * @param camera A câmera do jogador
     * @return O nível de detalhe (0 = máximo/tick normal, 1 = tick a cada 2 frames, etc.)
     */
    public static int getParticleLOD(Particle particle, Camera camera) {
        if (!BariumConfig.ENABLE_MOD_OPTIMIZATIONS || !BariumConfig.ENABLE_PARTICLE_LOD) {
            PARTICLE_LOD_LEVELS.put(particle, 0);
            return 0;
        }

        Vec3d particlePos = getParticlePosition(particle);
        Vec3d cameraPos = camera.getPos();

        double dx = particlePos.x - cameraPos.x;
        double dy = particlePos.y - cameraPos.y;
        double dz = particlePos.z - cameraPos.z;
        double distanceSq = dx * dx + dy * dy + dz * dz;

        int lod = 0;
        if (distanceSq > BariumConfig.PARTICLE_LOD_DISTANCE_START * BariumConfig.PARTICLE_LOD_DISTANCE_START) {
            double distance = Math.sqrt(distanceSq);
            lod = (int) ((distance - BariumConfig.PARTICLE_LOD_DISTANCE_START) / BariumConfig.PARTICLE_LOD_STEP_DISTANCE);
            lod = Math.min(lod, BariumConfig.MAX_PARTICLE_LOD_LEVELS);
            lod = Math.max(lod, 0);
        }

        PARTICLE_LOD_LEVELS.put(particle, lod);
        return lod;
    }

    /**
     * Verifica se uma partícula deve ser atualizada (tick) neste frame,
     * considerando o nível de LOD e a distância.
     *
     * @param particle A partícula
     * @param camera A câmera atual (para culling de distância e LOD)
     * @return true se a partícula deve ser tickada, false caso contrário (se deve ser ignorada ou expirada)
     */
    public static boolean shouldTickParticle(Particle particle, Camera camera) {
        if (!BariumConfig.ENABLE_MOD_OPTIMIZATIONS) {
            return true; // Não otimizar se o mod está desabilitado
        }

        // --- 1. Culling por Distância Completa (para Tick) ---
        // Se a otimização de distância está habilitada e a partícula está muito longe, ela deve expirar.
        if (BariumConfig.ENABLE_PARTICLE_CULLING) { // Usa a chave geral de culling para distância e frustum
            Vec3d particlePos = getParticlePosition(particle);
            Vec3d cameraPos = camera.getPos();

            double dx = particlePos.x - cameraPos.x;
            double dy = particlePos.y - cameraPos.y;
            double dz = particlePos.z - cameraPos.z;
            double distanceSq = dx * dx + dy * dy + dz * dz;

            if (distanceSq > BariumConfig.PARTICLE_CULLING_DISTANCE_SQ) {
                if (BariumConfig.ENABLE_DEBUG_LOGGING) {
                    BariumMod.LOGGER.debug("Culling (tick): Particle too far (" + Math.sqrt(distanceSq) + " blocks). Expiring.");
                }
                // Não retorna false aqui diretamente. A ParticleManagerMixin decidirá chamar expire().
                // A intenção é que se estiver muito longe, ela *deve* expirar.
                // Esta lógica de culling de distância se aplica tanto a tick quanto a renderização.
                return false; // Não tickar, deve ser expirada (e será)
            }
        }

        // --- 2. LOD para Tick ---
        // Se o LOD está habilitado e a partícula não está no LOD 0, verifica se ela deve tickar neste frame.
        if (BariumConfig.ENABLE_PARTICLE_LOD) {
            int lod = getParticleLOD(particle, camera); // Recalcula LOD se necessário (ou pega do mapa)
            if (lod > 0) {
                int counter = PARTICLE_TICK_COUNTERS.getOrDefault(particle, 0) + 1;
                PARTICLE_TICK_COUNTERS.put(particle, counter);

                if (counter % (lod + 1) != 0) { // Ex: LOD 1 = tick a cada 2 frames (1+1=2)
                    if (BariumConfig.ENABLE_DEBUG_LOGGING) {
                        BariumMod.LOGGER.debug("Culling (tick): Particle at LOD " + lod + " skipped tick.");
                    }
                    return false; // Pula o tick neste frame devido ao LOD
                }
            }
        }

        return true; // A partícula deve tickar
    }

    public static Vec3d getParticlePosition(Particle particle) {
        ParticleAccessor accessor = (ParticleAccessor) particle;
        return new Vec3d(accessor.barium$getX(), accessor.barium$getY(), accessor.barium$getZ());
    }

    /**
     * Verifica se uma partícula deve ser renderizada neste frame, considerando frustum culling e distância.
     *
     * @param particle A partícula
     * @param camera A câmera do jogador
     * @return true se a partícula deve ser renderizada, false caso contrário
     */
    public static boolean shouldRenderParticle(Particle particle, Camera camera) {
        if (!BariumConfig.ENABLE_MOD_OPTIMIZATIONS) {
            return true; // Não otimizar se o mod está desabilitado
        }

        // --- 1. Culling por Distância (Render) ---
        // Checa se a partícula está fora da distância máxima de renderização.
        if (BariumConfig.ENABLE_PARTICLE_CULLING) {
            Vec3d particlePos = getParticlePosition(particle);
            Vec3d cameraPos = camera.getPos();

            double dx = particlePos.x - cameraPos.x;
            double dy = particlePos.y - cameraPos.y;
            double dz = particlePos.z - cameraPos.z;
            double distanceSq = dx * dx + dy * dy + dz * dz;

            if (distanceSq > BariumConfig.PARTICLE_CULLING_DISTANCE_SQ) {
                if (BariumConfig.ENABLE_DEBUG_LOGGING) {
                    BariumMod.LOGGER.debug("Culling (render): Particle too far (" + Math.sqrt(distanceSq) + " blocks). Skipping render.");
                }
                return false; // Muito distante, não renderizar
            }

            // --- 2. Frustum Culling (Render) ---
            // Verifica se o BoundingBox da partícula está visível para a câmera.
            // O `isBoxVisible` da câmera é o método ideal para isso, pois já tem o frustum pré-calculado.
            // Para partículas que não têm BoundingBox (são apenas pontos ou muito pequenas),
            // this.isBoxVisible(particle.getBoundingBox()) pode ainda retornar true,
            // mas o clipping da GPU geralmente cuida disso.
            // O PARTICLE_CULLING_FOV_THRESHOLD não é usado aqui, pois isBoxVisible é mais robusto.
            if (!camera.isBoxVisible(particle.getBoundingBox())) {
                if (BariumConfig.ENABLE_DEBUG_LOGGING) {
                    BariumMod.LOGGER.debug("Culling (render): Particle outside frustum. Skipping render.");
                }
                return false; // Fora do campo de visão, não renderizar
            }
        }

        return true; // Deve ser renderizada
    }

    /**
     * Remove uma partícula do sistema de LOD e tick counter.
     * Chamado quando uma partícula expira ou é removida.
     *
     * @param particle A partícula a remover
     */
    public static void removeParticle(Particle particle) {
        PARTICLE_LOD_LEVELS.remove(particle);
        PARTICLE_TICK_COUNTERS.remove(particle);
        if (BariumConfig.ENABLE_DEBUG_LOGGING) {
            BariumMod.LOGGER.debug("Removed particle from optimizer maps.");
        }
    }
}