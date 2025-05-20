package com.barium.optimization;

import com.barium.BariumMod;
import com.barium.config.BariumConfig;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Otimizador de ticking de entidades distantes.
 * 
 * Implementa:
 * - Congelamento de entidades fora de uma zona de interesse
 */
public class EntityTickOptimizer {
    // Mapa para controlar o estado de congelamento das entidades
    private static final Map<UUID, Boolean> FROZEN_ENTITIES = new HashMap<>();
    
    // Último jogador mais próximo para cada entidade
    private static final Map<UUID, UUID> NEAREST_PLAYER = new HashMap<>();
    
    public static void init() {
        BariumMod.LOGGER.info("Inicializando otimizações de ticking de entidades distantes");
    }
    
    /**
     * Verifica se uma entidade deve ser "congelada" (ticking reduzido)
     * 
     * @param entity A entidade
     * @param world O mundo
     * @return true se a entidade deve ser congelada, false caso contrário
     */
    public static boolean shouldFreezeEntity(Entity entity, World world) {
        // Jogadores nunca são congelados
        if (entity instanceof PlayerEntity) {
            return false;
        }
        
        // Entidades com nomes específicos não são congeladas
        if (entity.hasCustomName() && entity.getCustomName().getString().contains("no_freeze")) {
            return false;
        }
        
        UUID entityId = entity.getUuid();
        
        // Verifica a distância até o jogador mais próximo
        PlayerEntity nearestPlayer = findNearestPlayer(entity, world);
        if (nearestPlayer == null) {
            // Se não houver jogadores, congela
            FROZEN_ENTITIES.put(entityId, true);
            return true;
        }
        
        // Atualiza o jogador mais próximo
        NEAREST_PLAYER.put(entityId, nearestPlayer.getUuid());
        
        // Calcula a distância
        double distance = entity.squaredDistanceTo(nearestPlayer);
        boolean shouldFreeze = distance > (BariumConfig.ENTITY_FREEZE_DISTANCE * BariumConfig.ENTITY_FREEZE_DISTANCE);
        
        // Atualiza o estado de congelamento
        FROZEN_ENTITIES.put(entityId, shouldFreeze);
        
        return shouldFreeze;
    }
    
    /**
     * Determina o nível de ticking para uma entidade congelada
     * 
     * @param entity A entidade
     * @return A frequência de ticking (1 = normal, maior = menos frequente)
     */
    public static int getFrozenTickRate(Entity entity) {
        UUID entityId = entity.getUuid();
        
        // Se a entidade não está congelada, tick normal
        if (!FROZEN_ENTITIES.getOrDefault(entityId, false)) {
            return 1;
        }
        
        // Entidades vivas têm um tick rate menor que outras entidades
        if (entity instanceof LivingEntity) {
            return 10; // Atualiza a cada 10 ticks
        } else {
            return 20; // Atualiza a cada 20 ticks
        }
    }
    
    /**
     * Verifica se uma entidade congelada deve ser atualizada neste tick
     * 
     * @param entity A entidade
     * @return true se a entidade deve ser atualizada, false caso contrário
     */
    public static boolean shouldTickFrozenEntity(Entity entity) {
        UUID entityId = entity.getUuid();
        
        // Se a entidade não está congelada, sempre atualiza
        if (!FROZEN_ENTITIES.getOrDefault(entityId, false)) {
            return true;
        }
        
        // Determina a frequência de atualização
        int tickRate = getFrozenTickRate(entity);
        
        // Verifica se é hora de atualizar
        return entity.age % tickRate == 0;
    }
    
    /**
     * Encontra o jogador mais próximo de uma entidade
     * 
     * @param entity A entidade
     * @param world O mundo
     * @return O jogador mais próximo, ou null se não houver jogadores
     */
    private static PlayerEntity findNearestPlayer(Entity entity, World world) {
        UUID entityId = entity.getUuid();
        
        // Verifica se já temos um jogador mais próximo registrado
        UUID lastPlayerId = NEAREST_PLAYER.get(entityId);
        if (lastPlayerId != null) {
            // Tenta encontrar o jogador pelo UUID
            for (PlayerEntity player : world.getPlayers()) {
                if (player.getUuid().equals(lastPlayerId)) {
                    // Verifica se o jogador ainda está próximo o suficiente
                    double distance = entity.squaredDistanceTo(player);
                    if (distance <= (BariumConfig.ENTITY_FREEZE_DISTANCE * BariumConfig.ENTITY_FREEZE_DISTANCE * 1.5)) {
                        return player;
                    }
                    break;
                }
            }
        }
        
        // Procura o jogador mais próximo
        PlayerEntity nearest = null;
        double minDistance = Double.MAX_VALUE;
        
        for (PlayerEntity player : world.getPlayers()) {
            double distance = entity.squaredDistanceTo(player);
            if (distance < minDistance) {
                minDistance = distance;
                nearest = player;
            }
        }
        
        return nearest;
    }
    
    /**
     * Remove uma entidade do sistema de congelamento
     * 
     * @param entityId UUID da entidade
     */
    public static void removeEntity(UUID entityId) {
        FROZEN_ENTITIES.remove(entityId);
        NEAREST_PLAYER.remove(entityId);
    }
}
