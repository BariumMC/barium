package com.barium.optimization;

import com.barium.BariumMod;
import com.barium.config.BariumConfig;
import net.minecraft.block.BlockState;
import net.minecraft.block.RedstoneWireBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

/**
 * Otimiza eventos de Redstone, limitando a propagação desnecessária e usando filas.
 * Baseado nos mappings Yarn 1.21.5+build.1
 */
public class RedstoneOptimizer {

    // Fila para atualizações de redstone compactadas
    private static final Queue<RedstoneUpdate> UPDATE_QUEUE = new ArrayDeque<>();
    private static final int MAX_QUEUE_SIZE = 1024;
    private static boolean processingQueue = false;

    // Conjunto para evitar atualizações recursivas imediatas no mesmo tick
    private static final Set<BlockPos> UPDATED_THIS_TICK = new HashSet<>();

    /**
     * Adiciona uma atualização de redstone à fila compactada em vez de processá-la imediatamente.
     *
     * @param world O mundo.
     * @param pos A posição do bloco que precisa ser atualizado.
     * @param sourcePos A posição do bloco que causou a atualização.
     * @return true se a atualização foi adicionada à fila, false se deve ser processada imediatamente.
     */
    public static boolean queueRedstoneUpdate(World world, BlockPos pos, BlockPos sourcePos) {
        if (!BariumConfig.ENABLE_REDSTONE_OPTIMIZATION || !BariumConfig.USE_COMPACT_QUEUE) {
            return false; // Processamento normal
        }

        // Evita adicionar se já atualizou neste tick (prevenção de recursão simples)
        if (UPDATED_THIS_TICK.contains(pos)) {
            return true; // Já na fila ou processado
        }

        // Adiciona à fila se não estiver cheia
        if (UPDATE_QUEUE.size() < MAX_QUEUE_SIZE) {
            UPDATE_QUEUE.offer(new RedstoneUpdate(world, pos, sourcePos));
            UPDATED_THIS_TICK.add(pos);
            return true; // Adicionado à fila
        } else {
            BariumMod.LOGGER.warn("Fila de atualização de Redstone cheia! Processando imediatamente.");
            return false; // Fila cheia, processa normalmente
        }
    }

    /**
     * Processa a fila de atualizações de redstone.
     * Deve ser chamado no final do tick do servidor/mundo.
     *
     * @param world O mundo.
     */
    public static void processUpdateQueue(World world) {
        if (processingQueue || UPDATE_QUEUE.isEmpty()) {
            return;
        }

        processingQueue = true;
        UPDATED_THIS_TICK.clear(); // Limpa para o próximo tick

        BariumMod.LOGGER.debug("Processando {} atualizações de redstone da fila.", UPDATE_QUEUE.size());

        try {
            while (!UPDATE_QUEUE.isEmpty()) {
                RedstoneUpdate update = UPDATE_QUEUE.poll();
                if (update != null && update.world == world) { // Garante que é o mundo correto
                    // Executa a atualização real do bloco
                    // A lógica exata depende de como o vanilla lida com updates
                    // Exemplo: notificar vizinhos ou recalcular estado
                    BlockState state = world.getBlockState(update.pos);
                    // Exemplo: world.updateNeighbor(update.pos, state.getBlock(), update.sourcePos);
                    // Exemplo: state.neighborUpdate(world, update.pos, world.getBlockState(update.sourcePos).getBlock(), update.sourcePos, false);
                    
                    // Placeholder para a lógica real de atualização
                    // Em um mod real, chamaríamos o método vanilla apropriado aqui
                    // world.updateNeighborsAlways(update.pos, world.getBlockState(update.pos).getBlock());
                }
            }
        } finally {
            processingQueue = false;
            // Limpa novamente em caso de erro
            if (!UPDATE_QUEUE.isEmpty()) {
                 BariumMod.LOGGER.error("Erro ao processar fila de redstone, limpando {} itens restantes.", UPDATE_QUEUE.size());
                 UPDATE_QUEUE.clear();
            }
            UPDATED_THIS_TICK.clear();
        }
    }

    /**
     * Limita a propagação desnecessária de sinais de redstone.
     * Verifica se a atualização realmente mudaria o estado do vizinho.
     *
     * @param world O mundo.
     * @param pos A posição do bloco sendo atualizado.
     * @param direction A direção do vizinho.
     * @param currentState O estado atual do bloco.
     * @return true se a propagação deve ser cancelada.
     */
    public static boolean limitRedstonePropagation(World world, BlockPos pos, Direction direction, BlockState currentState) {
        if (!BariumConfig.ENABLE_REDSTONE_OPTIMIZATION || !BariumConfig.LIMIT_SIGNAL_PROPAGATION) {
            return false;
        }

        BlockPos neighborPos = pos.offset(direction);
        BlockState neighborState = world.getBlockState(neighborPos);

        // Verifica se o vizinho é um fio de redstone
        if (neighborState.getBlock() instanceof RedstoneWireBlock) {
            int currentPower = currentState.getWeakRedstonePower(world, pos, direction);
            int neighborPower = neighborState.get(RedstoneWireBlock.POWER);

            // Calcula o poder que seria propagado
            // Lógica simplificada, a real é mais complexa
            int propagatedPower = Math.max(0, currentPower - 1);

            // Se o poder propagado não mudaria o estado do vizinho, cancela
            if (propagatedPower == neighborPower) {
                // Poderia adicionar verificações mais complexas aqui
                // return true; // Cancela a propagação
            }
        }
        
        // Adiciona mais verificações para outros blocos de redstone (repetidores, comparadores, etc.)
        // ...

        return false; // Permite a propagação por padrão
    }

    /**
     * Limpa o estado do otimizador (ex: ao descarregar um mundo).
     */
    public static void clearState() {
        UPDATE_QUEUE.clear();
        UPDATED_THIS_TICK.clear();
        processingQueue = false;
    }

    // --- Classe interna para a Fila de Atualização ---

    private static class RedstoneUpdate {
        final World world;
        final BlockPos pos;
        final BlockPos sourcePos;

        RedstoneUpdate(World world, BlockPos pos, BlockPos sourcePos) {
            this.world = world;
            this.pos = pos;
            this.sourcePos = sourcePos;
        }
    }
}
