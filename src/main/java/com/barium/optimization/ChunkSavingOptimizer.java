package com.barium.optimization;

import com.barium.BariumMod;
import com.barium.config.BariumConfig;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.storage.StorageIoWorker;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * Otimiza o salvamento de chunks (chunk saving), usando bufferização e compressão assíncrona.
 * Baseado nos mappings Yarn 1.21.5+build.1
 * Corrigido: Removida chamada a chunk.setNeedsSaving(false).
 */
public class ChunkSavingOptimizer {

    // Fila para chunks que precisam ser salvos
    private static final Queue<ChunkSaveOperation> SAVE_QUEUE = new LinkedList<>();
    // Usar valor da config
    private static final int MAX_BUFFER_SIZE = BariumConfig.CHUNK_SAVE_BUFFER_SIZE; 

    // Executor para compressão assíncrona (usar o executor do Minecraft se possível)
    private static Executor compressionExecutor = null;

    /**
     * Adiciona um chunk à fila de salvamento em buffer em vez de salvá-lo imediatamente.
     *
     * @param chunk O chunk a ser salvo.
     * @param world O mundo do servidor.
     * @param ioWorker O worker de I/O para salvamento.
     * @return true se o salvamento foi adicionado ao buffer, false se deve ser processado imediatamente.
     */
    public static boolean bufferChunkSave(WorldChunk chunk, ServerWorld world, StorageIoWorker ioWorker) {
        if (!BariumConfig.ENABLE_CHUNK_SAVING_OPTIMIZATION || !BariumConfig.BUFFER_CHUNK_SAVES) {
            return false; // Processamento normal
        }

        // Inicializa o executor na primeira vez (idealmente pegar do Minecraft)
        if (compressionExecutor == null && BariumConfig.ASYNC_CHUNK_COMPRESSION) {
            // Tenta obter um executor adequado do Minecraft ou cria um novo
            // Exemplo: compressionExecutor = Util.getMainWorkerExecutor();
            // Por enquanto, usaremos null para indicar que a compressão será síncrona se não configurado
            // TODO: Obter executor apropriado
        }

        synchronized (SAVE_QUEUE) {
            // Adiciona à fila apenas se precisar salvar
            if (chunk.needsSaving()) {
                SAVE_QUEUE.offer(new ChunkSaveOperation(chunk, world, ioWorker));
                // Se a fila atingir o tamanho máximo, força o processamento
                if (SAVE_QUEUE.size() >= MAX_BUFFER_SIZE) {
                    // BariumMod.LOGGER.debug("Buffer de salvamento de chunks cheio, processando...");
                    processSaveQueue(); // Processa a fila
                }
                return true; // Adicionado ao buffer
            } else {
                return false; // Não precisa salvar, não adiciona
            }
        }
    }

    /**
     * Processa a fila de salvamento de chunks.
     * Pode ser chamado periodicamente ou quando o buffer está cheio.
     */
    public static void processSaveQueue() {
        Queue<ChunkSaveOperation> queueToProcess;
        synchronized (SAVE_QUEUE) {
            if (SAVE_QUEUE.isEmpty()) {
                return;
            }
            // Copia a fila atual para processamento fora do lock
            queueToProcess = new LinkedList<>(SAVE_QUEUE);
            SAVE_QUEUE.clear();
        }

        BariumMod.LOGGER.debug("Processando {} chunks da fila de salvamento.", queueToProcess.size());

        // Processa cada operação de salvamento
        while (!queueToProcess.isEmpty()) {
            ChunkSaveOperation operation = queueToProcess.poll();
            if (operation != null) {
                saveChunkOperation(operation);
            }
        }
    }

    /**
     * Executa a operação de salvamento de um chunk, com compressão assíncrona opcional.
     *
     * @param operation A operação de salvamento.
     */
    private static void saveChunkOperation(ChunkSaveOperation operation) {
        WorldChunk chunk = operation.chunk;
        StorageIoWorker ioWorker = operation.ioWorker;

        // Verifica se o chunk ainda precisa ser salvo (pode ter sido descarregado ou salvo por outro meio)
        // A flag needsSaving() ainda é relevante para saber se *devemos* salvar.
        if (!chunk.needsSaving()) {
            return;
        }

        // Lógica de salvamento (baseada no vanilla)
        // A flag de 'needsSaving' é geralmente resetada pelo próprio processo de salvamento vanilla
        // ou pelo StorageIoWorker. Não precisamos chamar setNeedsSaving(false) manualmente aqui.
        // chunk.setNeedsSaving(false); // REMOVIDO - Método não existe / lógica incorreta
        ChunkPos chunkPos = chunk.getPos();

        try {
            // Prepara os dados para salvar (pode envolver serialização NBT)
            // net.minecraft.nbt.NbtCompound nbtData = ChunkSerializer.serialize(operation.world, chunk);
            // Placeholder para a lógica de serialização
            // TODO: Implementar serialização real usando ChunkSerializer ou equivalente
            Object nbtData = null; // Substituir pela serialização real

            if (nbtData != null) {
                // Compressão Assíncrona
                if (BariumConfig.ASYNC_CHUNK_COMPRESSION && compressionExecutor != null) {
                    // CompletableFuture.runAsync(() -> {
                    //     try {
                    //         // Comprime os dados NBT aqui
                    //         Object compressedData = compressData(nbtData);
                    //         // Envia para o IO worker
                    //         // TODO: Encontrar método correto em StorageIoWorker para 1.21.5
                    //         // ioWorker.writeChunk(chunkPos, compressedData);
                    //         // Ex: ioWorker.setResult(chunkPos, compressedData);
                    //     } catch (Exception e) {
                    //         BariumMod.LOGGER.error("Erro na compressão/escrita assíncrona do chunk {}: {}", chunkPos, e.getMessage());
                    //     }
                    // }, compressionExecutor);
                    
                    // Placeholder: por enquanto, salva síncrono mesmo se async estiver habilitado
                    // TODO: Implementar escrita real com StorageIoWorker API 1.21.5
                    // ioWorker.write(chunkPos, nbtData); // Método hipotético
                    BariumMod.LOGGER.warn("Salvamento assíncrono de chunk ainda não implementado, usando placeholder.");

                } else {
                    // Compressão e salvamento síncronos
                    // Object compressedData = compressData(nbtData);
                    // TODO: Implementar escrita real com StorageIoWorker API 1.21.5
                    // ioWorker.write(chunkPos, nbtData); // Método hipotético
                    BariumMod.LOGGER.warn("Salvamento síncrono de chunk ainda não implementado, usando placeholder.");
                }
            } else {
                 BariumMod.LOGGER.warn("Falha ao serializar chunk {} para salvamento.", chunkPos);
            }
        } catch (Exception e) {
            BariumMod.LOGGER.error("Erro ao salvar chunk {}: {}", chunkPos, e.getMessage());
        }
    }

    /**
     * Placeholder para a função de compressão de dados.
     */
    private static Object compressData(Object nbtData) {
        // Implementar compressão (ex: GZip, Zlib)
        return nbtData;
    }

    /**
     * Limpa a fila de salvamento (ex: ao parar o servidor).
     */
    public static void clearQueue() {
        processSaveQueue(); // Processa o que resta antes de limpar
        synchronized (SAVE_QUEUE) {
            SAVE_QUEUE.clear();
        }
    }

    // --- Classe interna para a Operação de Salvamento ---

    private static class ChunkSaveOperation {
        final WorldChunk chunk;
        final ServerWorld world;
        final StorageIoWorker ioWorker;

        ChunkSaveOperation(WorldChunk chunk, ServerWorld world, StorageIoWorker ioWorker) {
            this.chunk = chunk;
            this.world = world;
            this.ioWorker = ioWorker;
        }
    }
}

