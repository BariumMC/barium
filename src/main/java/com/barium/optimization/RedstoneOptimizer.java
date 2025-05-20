package com.barium.optimization;

import com.barium.BariumMod;
import com.barium.config.BariumConfig;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Otimizador de eventos de Redstone.
 * 
 * Implementa:
 * - Limitação da propagação desnecessária de sinais
 * - Fila compactada para atualizações ao invés de eventos recursivos imediatos
 */
public class RedstoneOptimizer {
    // Fila de atualizações de redstone pendentes
    private static final Map<World, Queue<RedstoneUpdate>> PENDING_UPDATES = new HashMap<>();
    
    // Conjunto para evitar atualizações duplicadas no mesmo tick
    private static final Map<World, Set<BlockPos>> ALREADY_UPDATED = new HashMap<>();
    
    // Contador de atualizações por tick
    private static final Map<World, Integer> UPDATE_COUNTERS = new HashMap<>();
    
    public static void init() {
        BariumMod.LOGGER.info("Inicializando otimizações de eventos de Redstone");
    }
    
    /**
     * Classe para representar uma atualização de redstone
     */
    private static class RedstoneUpdate {
        final BlockPos pos;
        final int priority;
        
        RedstoneUpdate(BlockPos pos, int priority) {
            this.pos = pos;
            this.priority = priority;
        }
    }
    
    /**
     * Verifica se uma atualização de redstone deve ser processada imediatamente
     * ou enfileirada para processamento posterior
     * 
     * @param world O mundo
     * @param pos A posição do bloco
     * @param oldPower O nível de energia anterior
     * @param newPower O novo nível de energia
     * @return true se a atualização deve ser processada imediatamente, false caso contrário
     */
    public static boolean shouldProcessRedstoneUpdateNow(World world, BlockPos pos, int oldPower, int newPower) {
        if (!BariumConfig.ENABLE_SIGNAL_COMPRESSION) {
            return true;
        }
        
        // Inicializa as estruturas para este mundo se necessário
        Queue<RedstoneUpdate> updates = PENDING_UPDATES.computeIfAbsent(world, k -> new ConcurrentLinkedQueue<>());
        Set<BlockPos> updated = ALREADY_UPDATED.computeIfAbsent(world, k -> new HashSet<>());
        int counter = UPDATE_COUNTERS.getOrDefault(world, 0);
        
        // Verifica se já atingimos o limite de atualizações por tick
        if (counter >= BariumConfig.MAX_REDSTONE_UPDATES_PER_TICK) {
            // Enfileira para o próximo tick se for importante
            if (Math.abs(oldPower - newPower) > 1) {
                queueRedstoneUpdate(world, pos, 1);
            }
            return false;
        }
        
        // Verifica se este bloco já foi atualizado neste tick
        if (updated.contains(pos)) {
            // Só processa imediatamente se for uma mudança significativa
            if (Math.abs(oldPower - newPower) > 2) {
                UPDATE_COUNTERS.put(world, counter + 1);
                return true;
            }
            return false;
        }
        
        // Marca como atualizado e incrementa o contador
        updated.add(pos.toImmutable());
        UPDATE_COUNTERS.put(world, counter + 1);
        return true;
    }
    
    /**
     * Enfileira uma atualização de redstone para processamento posterior
     * 
     * @param world O mundo
     * @param pos A posição do bloco
     * @param priority A prioridade da atualização (maior = mais importante)
     */
    public static void queueRedstoneUpdate(World world, BlockPos pos, int priority) {
        Queue<RedstoneUpdate> updates = PENDING_UPDATES.computeIfAbsent(world, k -> new ConcurrentLinkedQueue<>());
        updates.add(new RedstoneUpdate(pos.toImmutable(), priority));
    }
    
    /**
     * Processa as atualizações de redstone pendentes
     * Deve ser chamado no início de cada tick do mundo
     * 
     * @param world O mundo
     */
    public static void processQueuedUpdates(World world) {
        Queue<RedstoneUpdate> updates = PENDING_UPDATES.get(world);
        if (updates == null || updates.isEmpty()) {
            return;
        }
        
        // Limpa o conjunto de blocos já atualizados
        Set<BlockPos> updated = ALREADY_UPDATED.computeIfAbsent(world, k -> new HashSet<>());
        updated.clear();
        
        // Reseta o contador de atualizações
        UPDATE_COUNTERS.put(world, 0);
        
        // Processa as atualizações pendentes, limitando ao máximo por tick
        int processed = 0;
        while (!updates.isEmpty() && processed < BariumConfig.MAX_REDSTONE_UPDATES_PER_TICK) {
            RedstoneUpdate update = updates.poll();
            if (update != null && !updated.contains(update.pos)) {
                // Aqui seria chamado o código para atualizar o bloco
                // world.updateNeighborsAlways(update.pos, world.getBlockState(update.pos).getBlock());
                
                updated.add(update.pos);
                processed++;
            }
        }
    }
    
    /**
     * Verifica se um sinal de redstone deve se propagar para um bloco vizinho
     * 
     * @param world O mundo
     * @param source A posição da fonte do sinal
     * @param target A posição do alvo
     * @param power O nível de energia
     * @return true se o sinal deve se propagar, false caso contrário
     */
    public static boolean shouldPropagateSignal(World world, BlockPos source, BlockPos target, int power) {
        // Se o poder for muito baixo, não propaga
        if (power <= 0) {
            return false;
        }
        
        // Verifica se o bloco alvo já tem energia suficiente
        BlockState targetState = world.getBlockState(target);
        if (targetState.getWeakRedstonePower(world, target, null) >= power) {
            return false;
        }
        
        return true;
    }
}
