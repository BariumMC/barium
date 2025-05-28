package com.barium.optimization;

import com.barium.BariumMod;
import com.barium.config.BariumConfig;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Otimiza o ticking de blocos e tile entities, especialmente hoppers.
 * Introduz um sistema de ticking sob demanda e otimizações específicas.
 * Baseado nos mappings Yarn 1.21.5+build.1
 * Corrigido flags de configuração.
 */
public class BlockTickOptimizer {

    // Cache para o estado de hoppers (ativo/inativo)
    private static final Map<BlockPos, HopperState> HOPPER_STATE_CACHE = new ConcurrentHashMap<>();
    private static final long HOPPER_CACHE_DURATION_MS = 2000; // Cache válido por 2 segundos

    // Contador para ticking sob demanda
    private static final Map<BlockPos, Integer> DEMAND_TICK_COUNTER = new ConcurrentHashMap<>();
    private static final int DEMAND_TICK_THRESHOLD = 5; // Tick a cada 5 eventos de demanda

    /**
     * Verifica se um BlockEntity deve ter seu tick pulado com base no sistema sob demanda.
     *
     * @param blockEntity O BlockEntity.
     * @param world O mundo.
     * @return true se o tick deve ser pulado.
     */
    public static boolean shouldSkipTickDemandBased(BlockEntity blockEntity, World world) {
        if (!BariumConfig.ENABLE_BLOCK_TICK_OPTIMIZATION || !BariumConfig.USE_ON_DEMAND_TICKING) { // Corrected flag
            return false;
        }

        BlockPos pos = blockEntity.getPos();
        int demandCount = DEMAND_TICK_COUNTER.getOrDefault(pos, 0);

        // Só executa o tick se o contador atingir o limiar
        if (demandCount >= DEMAND_TICK_THRESHOLD) {
            DEMAND_TICK_COUNTER.put(pos, 0); // Reseta o contador
            return false; // Executa o tick
        } else {
            // Incrementa o contador apenas se houver um evento real (ex: mudança de redstone, inventário)
            // A lógica de incremento deve ser chamada externamente quando relevante
            return true; // Pula o tick
        }
    }

    /**
     * Registra um evento de demanda para um bloco, potencialmente ativando seu próximo tick.
     *
     * @param pos A posição do bloco.
     */
    public static void registerDemandTickEvent(BlockPos pos) {
        if (!BariumConfig.ENABLE_BLOCK_TICK_OPTIMIZATION || !BariumConfig.USE_ON_DEMAND_TICKING) { // Corrected flag
            return;
        }
        DEMAND_TICK_COUNTER.compute(pos, (k, v) -> (v == null) ? 1 : v + 1);
    }

    /**
     * Otimização específica para Hoppers: verifica se o hopper pode ser pulado.
     *
     * @param hopperEntity O HopperBlockEntity.
     * @param world O mundo.
     * @return true se o tick do hopper deve ser pulado.
     */
    public static boolean shouldSkipHopperTick(HopperBlockEntity hopperEntity, World world) {
        if (!BariumConfig.ENABLE_BLOCK_TICK_OPTIMIZATION || !BariumConfig.OPTIMIZE_HOPPERS) { // Corrected flag
            return false;
        }

        BlockPos pos = hopperEntity.getPos();
        HopperState cachedState = HOPPER_STATE_CACHE.get(pos);

        // Verifica o cache
        if (cachedState != null && cachedState.isValid()) {
            // Se o estado em cache indica inatividade, pula o tick
            if (!cachedState.isActive) {
                return true;
            }
        }

        // Verifica se o hopper está realmente ativo (tem itens para mover, espaço no destino, etc.)
        // Esta é uma verificação simplificada. Uma implementação real seria mais detalhada.
        boolean isActive = hopperNeedsProcessing(hopperEntity, world);

        // Atualiza o cache
        HOPPER_STATE_CACHE.put(pos, new HopperState(isActive));

        // Pula o tick se estiver inativo
        return !isActive;
    }

    /**
     * Verifica se um hopper precisa ser processado (lógica simplificada).
     *
     * @param hopperEntity O HopperBlockEntity.
     * @param world O mundo.
     * @return true se o hopper provavelmente precisa ser processado.
     */
    private static boolean hopperNeedsProcessing(HopperBlockEntity hopperEntity, World world) {
        // Verifica se está desligado por redstone
        if (!hopperEntity.getCachedState().get(net.minecraft.block.HopperBlock.ENABLED)) {
            return false;
        }

        // Verifica se tem itens para transferir
        boolean hasItemsToTransfer = false;
        for (int i = 0; i < hopperEntity.size(); ++i) {
            if (!hopperEntity.getStack(i).isEmpty()) {
                hasItemsToTransfer = true;
                break;
            }
        }
        if (!hasItemsToTransfer && !canPickupItems(hopperEntity, world)) {
             return false; // Não tem itens e não pode pegar
        }

        // Verifica se o inventário de destino tem espaço (simplificado)
        // Uma verificação real envolveria obter o inventário de destino e checar espaço
        boolean destinationHasSpace = true; // Assume que tem espaço por padrão

        return destinationHasSpace; // Precisa processar se tem itens e destino tem espaço
    }

    /**
     * Verifica se o hopper pode pegar itens de cima (lógica simplificada).
     */
    private static boolean canPickupItems(HopperBlockEntity hopperEntity, World world) {
        // Verifica se há um inventário ou entidade com itens acima
        // Lógica simplificada
        return true;
    }

    /**
     * Limpa todo o estado do otimizador (ex: ao fechar o mundo).
     */
    public static void clearAllStates() {
        HOPPER_STATE_CACHE.clear();
        DEMAND_TICK_COUNTER.clear();
    }

    // --- Classe interna para o Cache de Hopper ---

    private static class HopperState {
        final boolean isActive;
        final long timestamp;

        HopperState(boolean isActive) {
            this.isActive = isActive;
            this.timestamp = System.currentTimeMillis();
        }

        boolean isValid() {
            return (System.currentTimeMillis() - timestamp) < HOPPER_CACHE_DURATION_MS;
        }
    }
}

