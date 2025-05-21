package com.barium.client.optimization;

import com.barium.BariumMod;
import com.barium.config.BariumConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.chunk.ChunkBuilder;
import net.minecraft.client.render.chunk.ChunkRendererRegion;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.chunk.Chunk;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * Otimizador de geração de mesh de chunks com prioridade adaptativa.
 * 
 * Implementa:
 * - Sistema de prioridade adaptativa baseado na direção do olhar
 * - Paralelismo otimizado para geração de meshes
 * - Redução de congestionamento em áreas de construção rápida
 */
public class ChunkMeshPriorityOptimizer {
    // Fila de prioridade para chunks a serem processados
    private static final PriorityBlockingQueue<ChunkRenderTask> CHUNK_PRIORITY_QUEUE = 
            new PriorityBlockingQueue<>(100, Comparator.comparingDouble(ChunkRenderTask::getPriority).reversed());
    
    // Cache de prioridades por chunk
    private static final Map<ChunkPos, Double> CHUNK_PRIORITIES = new ConcurrentHashMap<>();
    
    // Timestamp da última atualização de prioridades
    private static long lastPriorityUpdate = 0;
    
    // Intervalo de atualização de prioridades em milissegundos
    private static final long PRIORITY_UPDATE_INTERVAL = 200;
    
    // Histórico de direção do olhar para detecção de movimento
    private static final Deque<Vec3d> LOOK_DIRECTION_HISTORY = new ArrayDeque<>();
    
    // Tamanho máximo do histórico de direção
    private static final int MAX_HISTORY_SIZE = 10;
    
    // Classe para representar uma tarefa de renderização de chunk com prioridade
    public static class ChunkRenderTask {
        private final ChunkBuilder.BuiltChunk chunk;
        private final double priority;
        
        public ChunkRenderTask(ChunkBuilder.BuiltChunk chunk, double priority) {
            this.chunk = chunk;
            this.priority = priority;
        }
        
        public ChunkBuilder.BuiltChunk getChunk() {
            return chunk;
        }
        
        public double getPriority() {
            return priority;
        }
    }
    
    public static void init() {
        BariumMod.LOGGER.info("Inicializando otimizações de prioridade de geração de mesh de chunks");
    }
    
    /**
     * Adiciona um chunk à fila de processamento com prioridade adaptativa
     * 
     * @param chunk O chunk a ser processado
     * @return true se o chunk foi adicionado com sucesso, false caso contrário
     */
    public static boolean scheduleChunkRender(ChunkBuilder.BuiltChunk chunk) {
        if (!BariumConfig.ENABLE_ADAPTIVE_CHUNK_MESH_PRIORITY) {
            return false; // Sem otimização, usa o sistema vanilla
        }
        
        // Calcula a prioridade do chunk
        double priority = calculateChunkPriority(chunk);
        
        // Cria a tarefa de renderização
        ChunkRenderTask task = new ChunkRenderTask(chunk, priority);
        
        // Adiciona à fila de prioridade
        return CHUNK_PRIORITY_QUEUE.offer(task);
    }
    
    /**
     * Obtém o próximo chunk a ser processado com base na prioridade
     * 
     * @return O próximo chunk a ser processado, ou null se a fila estiver vazia
     */
    public static ChunkBuilder.BuiltChunk getNextChunkToProcess() {
        // Atualiza as prioridades se necessário
        updatePriorities();
        
        // Obtém a próxima tarefa da fila
        ChunkRenderTask task = CHUNK_PRIORITY_QUEUE.poll();
        
        return task != null ? task.getChunk() : null;
    }
    
    /**
     * Calcula a prioridade de um chunk com base em vários fatores
     * 
     * @param chunk O chunk a ser avaliado
     * @return A prioridade calculada (maior = mais prioritário)
     */
    private static double calculateChunkPriority(ChunkBuilder.BuiltChunk chunk) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.gameRenderer == null || client.gameRenderer.getCamera() == null) {
            return 0.0; // Sem jogador ou câmera, prioridade zero
        }
        
        // Posição do chunk
        BlockPos origin = chunk.getOrigin();
        ChunkPos chunkPos = new ChunkPos(origin.getX() >> 4, origin.getZ() >> 4);
        
        // Verifica se já temos uma prioridade em cache
        Double cachedPriority = CHUNK_PRIORITIES.get(chunkPos);
        if (cachedPriority != null) {
            return cachedPriority;
        }
        
        // Posição da câmera
        Camera camera = client.gameRenderer.getCamera();
        Vec3d cameraPos = camera.getPos();
        
        // Centro do chunk
        Vec3d chunkCenter = new Vec3d(
                origin.getX() + 8,
                origin.getY() + 8,
                origin.getZ() + 8
        );
        
        // Distância ao quadrado da câmera ao chunk
        double distanceSq = cameraPos.squaredDistanceTo(chunkCenter);
        
        // Prioridade base: inversamente proporcional à distância
        double basePriority = 1000.0 / (1.0 + distanceSq);
        
        // Fator de direção do olhar
        double lookFactor = calculateLookFactor(chunkCenter, camera);
        
        // Fator de atividade (construção, quebra de blocos, etc.)
        double activityFactor = calculateActivityFactor(chunkPos);
        
        // Prioridade final
        double priority = basePriority * lookFactor * activityFactor;
        
        // Armazena no cache
        CHUNK_PRIORITIES.put(chunkPos, priority);
        
        return priority;
    }
    
    /**
     * Calcula um fator de prioridade baseado na direção do olhar
     * 
     * @param chunkCenter Centro do chunk
     * @param camera Câmera do jogador
     * @return Fator de prioridade (maior = mais alinhado com o olhar)
     */
    private static double calculateLookFactor(Vec3d chunkCenter, Camera camera) {
        // Posição da câmera
        Vec3d cameraPos = camera.getPos();
        
        // Direção da câmera
        float pitch = camera.getPitch();
        float yaw = camera.getYaw();
        Vec3d lookVec = Vec3d.fromPolar(pitch, yaw);
        
        // Vetor da câmera para o chunk
        Vec3d toChunk = chunkCenter.subtract(cameraPos).normalize();
        
        // Produto escalar para verificar o alinhamento
        double dot = toChunk.dotProduct(lookVec);
        
        // Converte para um fator de prioridade
        // 1.0 = olhando diretamente para o chunk
        // 0.1 = chunk está atrás do jogador
        return Math.max(0.1, (dot + 1.0) / 2.0 * 5.0);
    }
    
    /**
     * Calcula um fator de prioridade baseado na atividade recente no chunk
     * 
     * @param chunkPos Posição do chunk
     * @return Fator de atividade (maior = mais atividade)
     */
    private static double calculateActivityFactor(ChunkPos chunkPos) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) {
            return 1.0; // Sem mundo, fator neutro
        }
        
        // Verifica se o chunk está carregado
        if (!client.world.getChunkManager().isChunkLoaded(chunkPos.x, chunkPos.z)) {
            return 0.5; // Chunk não carregado, prioridade reduzida
        }
        
        // Obtém o chunk
        Chunk chunk = client.world.getChunk(chunkPos.x, chunkPos.z);
        
        // Verifica a idade do chunk (em ticks)
        long chunkAge = client.world.getTime() - chunk.getInhabitedTime();
        
        // Chunks mais novos ou recentemente modificados têm prioridade maior
        if (chunkAge < 20) {
            return 3.0; // Chunk muito recente
        } else if (chunkAge < 100) {
            return 2.0; // Chunk recente
        } else if (chunkAge < 600) {
            return 1.5; // Chunk moderadamente recente
        } else {
            return 1.0; // Chunk antigo
        }
    }
    
    /**
     * Atualiza as prioridades de todos os chunks na fila
     */
    private static void updatePriorities() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastPriorityUpdate < PRIORITY_UPDATE_INTERVAL) {
            return; // Ainda não é hora de atualizar
        }
        
        // Atualiza o timestamp
        lastPriorityUpdate = currentTime;
        
        // Limpa o cache de prioridades
        CHUNK_PRIORITIES.clear();
        
        // Atualiza o histórico de direção do olhar
        updateLookDirectionHistory();
        
        // Recria a fila com prioridades atualizadas
        PriorityBlockingQueue<ChunkRenderTask> newQueue = new PriorityBlockingQueue<>(
                CHUNK_PRIORITY_QUEUE.size() + 1,
                Comparator.comparingDouble(ChunkRenderTask::getPriority).reversed());
        
        // Transfere os chunks para a nova fila com prioridades atualizadas
        ChunkRenderTask task;
        while ((task = CHUNK_PRIORITY_QUEUE.poll()) != null) {
            double newPriority = calculateChunkPriority(task.getChunk());
            newQueue.offer(new ChunkRenderTask(task.getChunk(), newPriority));
        }
        
        // Substitui a fila antiga pela nova
        CHUNK_PRIORITY_QUEUE.clear();
        CHUNK_PRIORITY_QUEUE.addAll(newQueue);
    }
    
    /**
     * Atualiza o histórico de direção do olhar para detecção de movimento
     */
    private static void updateLookDirectionHistory() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.gameRenderer == null || client.gameRenderer.getCamera() == null) {
            return;
        }
        
        // Direção atual do olhar
        Camera camera = client.gameRenderer.getCamera();
        float pitch = camera.getPitch();
        float yaw = camera.getYaw();
        Vec3d lookVec = Vec3d.fromPolar(pitch, yaw);
        
        // Adiciona ao histórico
        LOOK_DIRECTION_HISTORY.addLast(lookVec);
        
        // Limita o tamanho do histórico
        while (LOOK_DIRECTION_HISTORY.size() > MAX_HISTORY_SIZE) {
            LOOK_DIRECTION_HISTORY.removeFirst();
        }
    }
    
    /**
     * Verifica se o jogador está movendo a câmera rapidamente
     * 
     * @return true se a câmera está em movimento rápido, false caso contrário
     */
    public static boolean isLookingAround() {
        if (LOOK_DIRECTION_HISTORY.size() < 2) {
            return false;
        }
        
        // Calcula a média de mudança de direção
        Iterator<Vec3d> iterator = LOOK_DIRECTION_HISTORY.iterator();
        Vec3d prev = iterator.next();
        double totalChange = 0.0;
        
        while (iterator.hasNext()) {
            Vec3d current = iterator.next();
            double dot = prev.dotProduct(current);
            double change = Math.acos(Math.max(-1.0, Math.min(1.0, dot)));
            totalChange += change;
            prev = current;
        }
        
        // Média de mudança por amostra
        double avgChange = totalChange / (LOOK_DIRECTION_HISTORY.size() - 1);
        
        // Considera movimento rápido se a média for maior que 0.05 radianos (~3 graus)
        return avgChange > 0.05;
    }
    
    /**
     * Ajusta o número de threads de renderização com base na carga atual
     * 
     * @param currentThreadCount Número atual de threads
     * @return Número ajustado de threads
     */
    public static int adjustRenderThreadCount(int currentThreadCount) {
        if (!BariumConfig.ENABLE_ADAPTIVE_THREAD_COUNT) {
            return currentThreadCount; // Sem ajuste
        }
        
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null || client.player == null) {
            return currentThreadCount;
        }
        
        // Número de chunks carregados
        int loadedChunks = 0;
        int renderDistance = client.options.getViewDistance();
        int playerChunkX = client.player.getBlockPos().getX() >> 4;
        int playerChunkZ = client.player.getBlockPos().getZ() >> 4;
        
        for (int x = playerChunkX - renderDistance; x <= playerChunkX + renderDistance; x++) {
            for (int z = playerChunkZ - renderDistance; z <= playerChunkZ + renderDistance; z++) {
                if (client.world.getChunkManager().isChunkLoaded(x, z)) {
                    loadedChunks++;
                }
            }
        }
        
        // Verifica se o jogador está se movendo rapidamente
        boolean isMovingFast = isLookingAround() || client.player.getVelocity().lengthSquared() > 0.1;
        
        // Ajusta o número de threads
        int maxThreads = Runtime.getRuntime().availableProcessors() - 1;
        int minThreads = 1;
        
        if (isMovingFast) {
            // Mais threads durante movimento rápido
            return Math.min(maxThreads, currentThreadCount + 1);
        } else if (loadedChunks < renderDistance * renderDistance / 2) {
            // Menos threads quando poucos chunks estão carregados
            return Math.max(minThreads, currentThreadCount - 1);
        } else {
            // Mantém o número atual de threads
            return currentThreadCount;
        }
    }
    
    /**
     * Limpa os caches e filas
     */
    public static void clearCaches() {
        CHUNK_PRIORITIES.clear();
        CHUNK_PRIORITY_QUEUE.clear();
        LOOK_DIRECTION_HISTORY.clear();
        lastPriorityUpdate = 0;
    }
}
