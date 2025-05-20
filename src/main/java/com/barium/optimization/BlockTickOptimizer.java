package com.barium.optimization;

import com.barium.BariumMod;
import com.barium.config.BariumConfig;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Otimizador de ticking de blocos e tile entities.
 * 
 * Implementa:
 * - Sistema de ticking sob demanda
 * - Otimizações específicas para hoppers
 */
public class BlockTickOptimizer {
    // Conjunto de posições de blocos que precisam ser atualizados
    private static final Map<World, Set<BlockPos>> BLOCKS_TO_UPDATE = new HashMap<>();
    
    // Mapa de blocos que foram modificados recentemente
    private static final Map<BlockPos, Integer> RECENTLY_MODIFIED = new HashMap<>();
    
    // Contador para limitar verificações de hoppers
    private static final Map<BlockPos, Integer> HOPPER_COUNTERS = new HashMap<>();
    
    public static void init() {
        BariumMod.LOGGER.info("Inicializando otimizações de ticking de blocos e tile entities");
    }
    
    /**
     * Verifica se um bloco deve ser atualizado neste tick
     * 
     * @param world O mundo
     * @param pos A posição do bloco
     * @param state O estado do bloco
     * @return true se o bloco deve ser atualizado, false caso contrário
     */
    public static boolean shouldTickBlock(World world, BlockPos pos, BlockState state) {
        if (!BariumConfig.ENABLE_ON_DEMAND_TICKING) {
            return true;
        }
        
        // Verifica se o bloco está na lista de atualizações pendentes
        Set<BlockPos> worldBlocks = BLOCKS_TO_UPDATE.getOrDefault(world, new HashSet<>());
        if (worldBlocks.contains(pos)) {
            worldBlocks.remove(pos);
            return true;
        }
        
        // Verifica se o bloco foi modificado recentemente
        if (RECENTLY_MODIFIED.containsKey(pos)) {
            int ticksLeft = RECENTLY_MODIFIED.get(pos) - 1;
            if (ticksLeft <= 0) {
                RECENTLY_MODIFIED.remove(pos);
            } else {
                RECENTLY_MODIFIED.put(pos, ticksLeft);
                return true;
            }
        }
        
        // Alguns blocos sempre precisam ser atualizados
        if (state.hasRandomTicks()) {
            return true;
        }
        
        return false;
    }
    
    /**
     * Verifica se um hopper deve ser atualizado neste tick
     * 
     * @param hopper O hopper
     * @param pos A posição do hopper
     * @return true se o hopper deve ser atualizado, false caso contrário
     */
    public static boolean shouldTickHopper(HopperBlockEntity hopper, BlockPos pos) {
        if (BariumConfig.HOPPER_OPTIMIZATION_LEVEL <= 0) {
            return true;
        }
        
        // Nível 1: Reduz a frequência de verificações
        if (BariumConfig.HOPPER_OPTIMIZATION_LEVEL == 1) {
            int counter = HOPPER_COUNTERS.getOrDefault(pos, 0);
            counter++;
            
            if (counter >= 2) { // Verifica a cada 2 ticks
                HOPPER_COUNTERS.put(pos, 0);
                return true;
            } else {
                HOPPER_COUNTERS.put(pos, counter);
                return false;
            }
        }
        
        // Nível 2: Verifica apenas quando há mudanças no inventário
        if (BariumConfig.HOPPER_OPTIMIZATION_LEVEL >= 2) {
            // Se o hopper foi marcado para atualização, permite o tick
            Set<BlockPos> worldBlocks = BLOCKS_TO_UPDATE.getOrDefault(hopper.getWorld(), new HashSet<>());
            if (worldBlocks.contains(pos)) {
                worldBlocks.remove(pos);
                return true;
            }
            
            // Caso contrário, reduz a frequência drasticamente
            int counter = HOPPER_COUNTERS.getOrDefault(pos, 0);
            counter++;
            
            if (counter >= 10) { // Verifica a cada 10 ticks para garantir
                HOPPER_COUNTERS.put(pos, 0);
                return true;
            } else {
                HOPPER_COUNTERS.put(pos, counter);
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Marca um bloco para ser atualizado no próximo tick
     * 
     * @param world O mundo
     * @param pos A posição do bloco
     */
    public static void scheduleBlockUpdate(World world, BlockPos pos) {
        Set<BlockPos> worldBlocks = BLOCKS_TO_UPDATE.computeIfAbsent(world, k -> new HashSet<>());
        worldBlocks.add(pos.toImmutable());
    }
    
    /**
     * Marca um bloco como modificado recentemente
     * 
     * @param pos A posição do bloco
     * @param ticks Número de ticks para continuar atualizando
     */
    public static void markBlockModified(BlockPos pos, int ticks) {
        RECENTLY_MODIFIED.put(pos.toImmutable(), ticks);
    }
    
    /**
     * Marca os blocos vizinhos para atualização
     * 
     * @param world O mundo
     * @param pos A posição central
     */
    public static void scheduleNeighborUpdates(World world, BlockPos pos) {
        scheduleBlockUpdate(world, pos.north());
        scheduleBlockUpdate(world, pos.south());
        scheduleBlockUpdate(world, pos.east());
        scheduleBlockUpdate(world, pos.west());
        scheduleBlockUpdate(world, pos.up());
        scheduleBlockUpdate(world, pos.down());
    }
}
