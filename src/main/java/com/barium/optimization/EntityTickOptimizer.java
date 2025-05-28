package com.barium.optimization;

import com.barium.BariumMod;
import com.barium.config.BariumConfig;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Otimiza o ticking de entidades distantes, congelando-as ou reduzindo a frequência.
 * Baseado nos mappings Yarn 1.21.5+build.1
 * Corrigido para remover dependências do cliente.
 */
public class EntityTickOptimizer {

    // Distância quadrada para considerar uma entidade "distante"
    private static final double FAR_ENTITY_DISTANCE_SQ = BariumConfig.ENTITY_REDUCED_TICK_DISTANCE * BariumConfig.ENTITY_REDUCED_TICK_DISTANCE; // Use config
    // Distância quadrada para congelar completamente entidades (exceto jogadores)
    private static final double FREEZE_ENTITY_DISTANCE_SQ = BariumConfig.ENTITY_FREEZE_DISTANCE * BariumConfig.ENTITY_FREEZE_DISTANCE; // Use config

    // Frequência de tick para entidades distantes (1 tick a cada N ticks)
    private static final int FAR_ENTITY_TICK_INTERVAL = 10; // Tick a cada 0.5 segundos (Pode ser configurável)
    private static final int FROZEN_ENTITY_TICK_INTERVAL = 100; // Tick a cada 5 segundos para entidades congeladas

    // Cache para o estado de tick das entidades
    private static final Map<Entity, TickState> ENTITY_TICK_STATE = new ConcurrentHashMap<>();

    /**
     * Verifica se o tick de uma entidade deve ser pulado com base na distância.
     *
     * @param entity A entidade.
     * @param world O mundo (deve ser ServerWorld para esta lógica).
     * @return true se o tick deve ser pulado.
     */
    public static boolean shouldSkipEntityTick(Entity entity, World world) {
        // Esta otimização só faz sentido no lado do servidor
        if (!world.isClient() && BariumConfig.ENABLE_ENTITY_TICK_OPTIMIZATION && !(entity instanceof PlayerEntity)) {
            // Obtém o estado de tick da entidade
            // TickState state = ENTITY_TICK_STATE.computeIfAbsent(entity, k -> new TickState()); // Estado não usado atualmente

            // Verifica se a entidade está muito longe para ser congelada
            if (BariumConfig.FREEZE_FAR_ENTITIES && isEntityTooFarFromServer(entity, (ServerWorld) world, FREEZE_ENTITY_DISTANCE_SQ)) {
                // Congela a entidade (pula a maioria dos ticks)
                if ((world.getTime() + entity.getId()) % FROZEN_ENTITY_TICK_INTERVAL != 0) {
                    // BariumMod.LOGGER.debug("Freezing entity {}", entity.getId());
                    return true; // Pula o tick
                }
            }

            // Verifica se a entidade está distante para reduzir a frequência de tick
            if (BariumConfig.REDUCE_FAR_ENTITY_TICKS && isEntityTooFarFromServer(entity, (ServerWorld) world, FAR_ENTITY_DISTANCE_SQ)) {
                // Reduz a frequência de tick
                if ((world.getTime() + entity.getId()) % FAR_ENTITY_TICK_INTERVAL != 0) {
                    // BariumMod.LOGGER.debug("Skipping tick for far entity {}", entity.getId());
                    return true; // Pula o tick
                }
            }
        }

        // Entidade próxima, jogador, otimização desligada ou tick permitido, executa normalmente
        return false;
    }

    /**
     * Verifica se uma entidade está além de uma certa distância quadrada de qualquer jogador no servidor.
     *
     * @param entity A entidade.
     * @param serverWorld O mundo do servidor.
     * @param distanceSq A distância quadrada limite.
     * @return true se a entidade está muito longe de todos os jogadores.
     */
    private static boolean isEntityTooFarFromServer(Entity entity, ServerWorld serverWorld, double distanceSq) {
        // Se não houver jogadores, considera longe por padrão?
        if (serverWorld.getPlayers().isEmpty()) {
            return true; 
        }
        for (PlayerEntity player : serverWorld.getPlayers()) {
            // Ignora jogadores em modo espectador ou criativo para cálculo de distância?
            // if (player.isSpectator() || player.isCreative()) continue;
            if (entity.squaredDistanceTo(player) <= distanceSq) {
                return false; // Perto de pelo menos um jogador
            }
        }
        return true; // Longe de todos os jogadores relevantes
    }

    /**
     * Limpa o estado de uma entidade (ex: quando descarregada).
     *
     * @param entity A entidade.
     */
    public static void clearEntityState(Entity entity) {
        ENTITY_TICK_STATE.remove(entity);
    }

    /**
     * Limpa todo o estado do otimizador (ex: ao fechar o mundo).
     */
    public static void clearAllStates() {
        ENTITY_TICK_STATE.clear();
    }

    // --- Classe interna para o Estado de Tick ---

    private static class TickState {
        // Poderia armazenar informações adicionais aqui se necessário,
        // como o último tick executado, etc.
        // Por enquanto, a presença no mapa é suficiente.
    }
}

