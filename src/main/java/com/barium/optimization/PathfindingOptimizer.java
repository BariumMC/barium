package com.barium.optimization;

import com.barium.BariumMod;
import com.barium.config.BariumConfig;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Otimiza o pathfinding de mobs para reduzir o impacto no desempenho.
 * Utiliza caching de rotas e atualizações menos frequentes para mobs distantes.
 * Baseado nos mappings Yarn 1.21.5+build.1
 */
public class PathfindingOptimizer {

    // Cache para rotas calculadas recentemente
    // Usamos WeakHashMap para permitir que as entidades sejam coletadas pelo GC
    private static final Map<MobEntity, PathCacheEntry> PATH_CACHE = new WeakHashMap<>();
    private static final long CACHE_DURATION_MS = 5000; // Cache válido por 5 segundos

    // Cache para verificações de colisão simplificadas (mobs em repouso)
    private static final Map<MobEntity, CollisionCacheEntry> COLLISION_CACHE = new WeakHashMap<>();
    private static final long COLLISION_CACHE_DURATION_MS = 1000; // Cache válido por 1 segundo

    // Limite de distância para otimizações mais agressivas
    private static final double FAR_MOB_DISTANCE_SQ = BariumConfig.PATH_UPDATE_DISTANCE * BariumConfig.PATH_UPDATE_DISTANCE; // Use config

    // Frequência de atualização para mobs distantes (em ticks)
    private static final int FAR_MOB_UPDATE_INTERVAL = BariumConfig.PATH_UPDATE_INTERVAL_TICKS; // Use config

    /**
     * Tenta obter uma rota do cache antes de calcular uma nova.
     *
     * @param entity A entidade que está navegando.
     * @param target A posição alvo.
     * @param range O alcance máximo da rota.
     * @return O Path em cache, ou null se não houver cache válido.
     */
    public static Path getCachedPath(MobEntity entity, BlockPos target, float range) {
        if (!BariumConfig.ENABLE_PATHFINDING_OPTIMIZATION || !BariumConfig.USE_SMART_CACHE) { // Corrected flag
            return null;
        }

        PathCacheEntry entry = PATH_CACHE.get(entity);
        if (entry != null && entry.isValid(target, range)) {
            BariumMod.LOGGER.debug("Path cache hit for entity {}", entity.getId());
            return entry.path;
        }
        return null;
    }

    /**
     * Armazena uma rota recém-calculada no cache.
     *
     * @param entity A entidade.
     * @param target A posição alvo.
     * @param range O alcance.
     * @param path O Path calculado.
     */
    public static void cachePath(MobEntity entity, BlockPos target, float range, Path path) {
        if (!BariumConfig.ENABLE_PATHFINDING_OPTIMIZATION || !BariumConfig.USE_SMART_CACHE) { // Corrected flag
            return;
        }

        if (path != null) {
            PATH_CACHE.put(entity, new PathCacheEntry(target, range, path));
            BariumMod.LOGGER.debug("Path cached for entity {}", entity.getId());
        }
    }

    /**
     * Verifica se a entidade deve ter seu pathfinding atualizado com menos frequência.
     *
     * @param entity A entidade.
     * @param world O mundo.
     * @return true se a atualização deve ser pulada.
     */
    public static boolean shouldSkipPathfindingUpdate(MobEntity entity, World world) {
        if (!BariumConfig.ENABLE_PATHFINDING_OPTIMIZATION || !BariumConfig.REDUCE_OFFSCREEN_PATHFINDING) { // Corrected flag
            return false;
        }

        // Verifica a distância do jogador mais próximo
        Entity nearestPlayer = world.getClosestPlayer(entity, -1.0D);
        if (nearestPlayer == null) {
            return true; // Sem jogadores por perto, otimiza
        }

        double distanceSq = entity.squaredDistanceTo(nearestPlayer);
        if (distanceSq > FAR_MOB_DISTANCE_SQ) {
            // Mob está longe, atualiza com menos frequência
            // Usa o tick do mundo e o ID da entidade para distribuir as atualizações
            return (world.getTime() + entity.getId()) % FAR_MOB_UPDATE_INTERVAL != 0;
        }

        return false;
    }

    /**
     * Realiza uma verificação de colisão simplificada para mobs em repouso.
     *
     * @param entity A entidade.
     * @return true se a entidade provavelmente não colidiu (baseado no cache ou verificação simples).
     */
    public static boolean simplifiedCollisionCheck(MobEntity entity) {
        if (!BariumConfig.ENABLE_PATHFINDING_OPTIMIZATION || !BariumConfig.SIMPLIFY_COLLISION) { // Corrected flag
            return false; // Indica que a verificação completa deve ser feita
        }

        // Verifica se a entidade está se movendo significativamente
        Vec3d velocity = entity.getVelocity();
        if (velocity.lengthSquared() > 0.01) { // Limiar pequeno para detectar movimento
            COLLISION_CACHE.remove(entity); // Remove do cache se começou a mover
            return false; // Movendo, precisa de verificação completa
        }

        // Verifica o cache de colisão
        CollisionCacheEntry entry = COLLISION_CACHE.get(entity);
        if (entry != null && entry.isValid(entity.getBlockPos())) {
            return entry.canMove; // Retorna resultado do cache
        }

        // Realiza uma verificação simplificada (ex: apenas blocos adjacentes)
        // Esta é uma lógica placeholder, uma implementação real seria mais complexa
        World world = entity.getWorld();
        BlockPos currentPos = entity.getBlockPos();
        boolean canMove = world.getBlockState(currentPos.down()).isAir() || // Pode cair?
                          world.getBlockState(currentPos).isAir(); // Está no ar?

        // Armazena no cache
        COLLISION_CACHE.put(entity, new CollisionCacheEntry(currentPos, canMove));

        return canMove;
    }

    // --- Classes internas para o Cache ---

    private static class PathCacheEntry {
        final BlockPos target;
        final float range;
        final Path path;
        final long timestamp;

        PathCacheEntry(BlockPos target, float range, Path path) {
            this.target = target;
            this.range = range;
            this.path = path;
            this.timestamp = System.currentTimeMillis();
        }

        boolean isValid(BlockPos currentTarget, float currentRange) {
            return target.equals(currentTarget) &&
                   Math.abs(range - currentRange) < 0.1f &&
                   (System.currentTimeMillis() - timestamp) < CACHE_DURATION_MS;
        }
    }

    private static class CollisionCacheEntry {
        final BlockPos position;
        final boolean canMove;
        final long timestamp;

        CollisionCacheEntry(BlockPos position, boolean canMove) {
            this.position = position;
            this.canMove = canMove;
            this.timestamp = System.currentTimeMillis();
        }

        boolean isValid(BlockPos currentPosition) {
            return position.equals(currentPosition) &&
                   (System.currentTimeMillis() - timestamp) < COLLISION_CACHE_DURATION_MS;
        }
    }
}

