package com.barium.optimization;

import com.barium.BariumMod;
import com.barium.config.BariumConfig;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.BlockPos;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Otimizador de pathfinding e IA de mobs.
 * 
 * Implementa:
 * - Cache inteligente de rotas
 * - Simplificação de verificação de colisão para mobs em repouso
 * - Atualizações menos frequentes para mobs fora do foco do jogador
 */
public class PathfindingOptimizer {
    // Cache de rotas: UUID do mob -> (destino -> caminho)
    private static final Map<UUID, Map<BlockPos, Object>> PATH_CACHE = new HashMap<>();
    
    // Contador de ticks para controlar a frequência de atualizações
    private static final Map<UUID, Integer> UPDATE_COUNTERS = new HashMap<>();
    
    public static void init() {
        BariumMod.LOGGER.info("Inicializando otimizações de pathfinding e IA de mobs");
    }
    
    /**
     * Verifica se um mob deve ter seu pathfinding atualizado neste tick
     * 
     * @param entity A entidade mob
     * @param playerDistance Distância até o jogador mais próximo
     * @return true se o pathfinding deve ser atualizado, false caso contrário
     */
    public static boolean shouldUpdatePathfinding(MobEntity entity, double playerDistance) {
        UUID entityId = entity.getUuid();
        
        // Incrementa o contador de ticks para esta entidade
        int counter = UPDATE_COUNTERS.getOrDefault(entityId, 0) + 1;
        UPDATE_COUNTERS.put(entityId, counter);
        
        // Determina a frequência de atualização com base na distância do jogador
        int updateInterval = 1;
        if (playerDistance > BariumConfig.PATH_UPDATE_DISTANCE) {
            updateInterval = BariumConfig.PATH_UPDATE_INTERVAL_TICKS;
        }
        
        // Verifica se é hora de atualizar
        boolean shouldUpdate = counter >= updateInterval;
        if (shouldUpdate) {
            UPDATE_COUNTERS.put(entityId, 0);
        }
        
        return shouldUpdate;
    }
    
    /**
     * Verifica se um caminho está em cache e o retorna
     * 
     * @param entity A entidade mob
     * @param target O destino alvo
     * @return O caminho em cache, ou null se não estiver em cache
     */
    public static Object getCachedPath(MobEntity entity, BlockPos target) {
        UUID entityId = entity.getUuid();
        Map<BlockPos, Object> entityCache = PATH_CACHE.get(entityId);
        
        if (entityCache != null) {
            return entityCache.get(target);
        }
        
        return null;
    }
    
    /**
     * Armazena um caminho no cache
     * 
     * @param entity A entidade mob
     * @param target O destino alvo
     * @param path O caminho a ser armazenado
     */
    public static void cachePath(MobEntity entity, BlockPos target, Object path) {
        UUID entityId = entity.getUuid();
        
        // Cria o cache para esta entidade se não existir
        Map<BlockPos, Object> entityCache = PATH_CACHE.computeIfAbsent(entityId, k -> 
            new LinkedHashMap<BlockPos, Object>(BariumConfig.PATH_CACHE_SIZE, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<BlockPos, Object> eldest) {
                    return size() > BariumConfig.PATH_CACHE_SIZE;
                }
            }
        );
        
        // Armazena o caminho
        entityCache.put(target, path);
    }
    
    /**
     * Simplifica a verificação de colisão para mobs em repouso
     * 
     * @param entity A entidade mob
     * @return true se a verificação de colisão completa deve ser ignorada
     */
    public static boolean shouldSkipCollisionCheck(MobEntity entity) {
        // Verifica se o mob está em repouso (não se movendo)
        if (entity.getVelocity().lengthSquared() < 0.0001) {
            // Verifica se não há jogadores próximos que possam interagir
            // Simplificado para verificar apenas se está longe de jogadores
            if (entity.getWorld().getClosestPlayer(entity, 8.0) == null) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Limpa o cache de caminhos para uma entidade específica
     * 
     * @param entityId UUID da entidade
     */
    public static void clearPathCache(UUID entityId) {
        PATH_CACHE.remove(entityId);
        UPDATE_COUNTERS.remove(entityId);
    }
}
