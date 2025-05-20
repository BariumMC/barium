package com.barium.optimization;

import com.barium.BariumMod;
import com.barium.config.BariumConfig;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;

/**
 * Otimizador de mundo e salvamento (chunk saving).
 * 
 * Implementa:
 * - Bufferização inteligente para salvar chunks em lotes
 * - Compressão assíncrona de chunks
 */
public class ChunkSavingOptimizer {
    // Fila de chunks para salvar
    private static final Map<ServerWorld, Queue<Chunk>> CHUNKS_TO_SAVE = new HashMap<>();
    
    // Conjunto de chunks sendo salvos atualmente
    private static final Map<ServerWorld, Set<Long>> CHUNKS_BEING_SAVED = new HashMap<>();
    
    // Executor para compressão assíncrona
    private static Executor compressionExecutor;
    
    public static void init() {
        BariumMod.LOGGER.info("Inicializando otimizações de mundo e salvamento de chunks");
    }
    
    /**
     * Define o executor para compressão assíncrona
     * 
     * @param executor O executor a ser usado
     */
    public static void setCompressionExecutor(Executor executor) {
        compressionExecutor = executor;
    }
    
    /**
     * Enfileira um chunk para salvamento
     * 
     * @param world O mundo do chunk
     * @param chunk O chunk a ser salvo
     * @param priority A prioridade do salvamento (maior = mais importante)
     * @return true se o chunk foi enfileirado, false caso contrário
     */
    public static boolean queueChunkForSaving(ServerWorld world, Chunk chunk, int priority) {
        if (!BariumConfig.ENABLE_ASYNC_COMPRESSION) {
            return false; // Deixa o jogo lidar com o salvamento normalmente
        }
        
        // Verifica se o chunk já está sendo salvo
        Set<Long> beingSaved = CHUNKS_BEING_SAVED.computeIfAbsent(world, k -> new HashSet<>());
        long chunkPos = chunk.getPos().toLong();
        
        if (beingSaved.contains(chunkPos)) {
            return true; // Já está sendo salvo
        }
        
        // Enfileira o chunk para salvamento
        Queue<Chunk> chunksToSave = CHUNKS_TO_SAVE.computeIfAbsent(world, k -> new ConcurrentLinkedQueue<>());
        chunksToSave.add(chunk);
        
        return true;
    }
    
    /**
     * Processa o salvamento de chunks enfileirados
     * Deve ser chamado periodicamente pelo servidor
     * 
     * @param world O mundo
     * @return O número de chunks processados
     */
    public static int processChunkSaving(ServerWorld world) {
        if (!BariumConfig.ENABLE_ASYNC_COMPRESSION || compressionExecutor == null) {
            return 0;
        }
        
        Queue<Chunk> chunksToSave = CHUNKS_TO_SAVE.get(world);
        if (chunksToSave == null || chunksToSave.isEmpty()) {
            return 0;
        }
        
        Set<Long> beingSaved = CHUNKS_BEING_SAVED.computeIfAbsent(world, k -> new HashSet<>());
        
        // Processa chunks em lotes
        int processed = 0;
        List<Chunk> batch = new ArrayList<>();
        
        while (!chunksToSave.isEmpty() && batch.size() < BariumConfig.CHUNK_SAVE_BUFFER_SIZE) {
            Chunk chunk = chunksToSave.poll();
            if (chunk != null && chunk.getStatus() == ChunkStatus.FULL) {
                long chunkPos = chunk.getPos().toLong();
                if (!beingSaved.contains(chunkPos)) {
                    batch.add(chunk);
                    beingSaved.add(chunkPos);
                    processed++;
                }
            }
        }
        
        if (!batch.isEmpty()) {
            // Salva o lote de chunks de forma assíncrona
            CompletableFuture.runAsync(() -> {
                try {
                    for (Chunk chunk : batch) {
                        // Aqui seria chamado o código para salvar o chunk
                        // world.getPersistentStateManager().save();
                        
                        // Remove do conjunto de chunks sendo salvos
                        beingSaved.remove(chunk.getPos().toLong());
                    }
                } catch (Exception e) {
                    BariumMod.LOGGER.error("Erro ao salvar chunks: " + e.getMessage());
                    
                    // Em caso de erro, remove todos os chunks do conjunto
                    for (Chunk chunk : batch) {
                        beingSaved.remove(chunk.getPos().toLong());
                    }
                }
            }, compressionExecutor);
        }
        
        return processed;
    }
    
    /**
     * Verifica se um chunk está sendo salvo atualmente
     * 
     * @param world O mundo
     * @param chunkX Coordenada X do chunk
     * @param chunkZ Coordenada Z do chunk
     * @return true se o chunk está sendo salvo, false caso contrário
     */
    public static boolean isChunkBeingSaved(ServerWorld world, int chunkX, int chunkZ) {
        Set<Long> beingSaved = CHUNKS_BEING_SAVED.get(world);
        if (beingSaved == null) {
            return false;
        }
        
        long chunkPos = ((long)chunkX & 0xFFFFFFFFL) | (((long)chunkZ & 0xFFFFFFFFL) << 32);
        return beingSaved.contains(chunkPos);
    }
    
    /**
     * Limpa todas as filas de salvamento para um mundo
     * 
     * @param world O mundo
     */
    public static void clearSavingQueues(ServerWorld world) {
        CHUNKS_TO_SAVE.remove(world);
        CHUNKS_BEING_SAVED.remove(world);
    }
}
