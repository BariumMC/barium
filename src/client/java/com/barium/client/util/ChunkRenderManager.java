package com.barium.client.util;

import com.barium.config.BariumConfig; // Importar BariumConfig
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;

import java.util.BitSet;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Gerencia o cálculo de quais chunks devem ser renderizados com base na posição e visão do jogador.
 * Renomeado de ChunkCalculator para refletir sua função de gerenciamento de estado de renderização de chunks.
 */
public class ChunkRenderManager {
    /** O raio de chunks para sempre renderizar ao redor do jogador. */
    private static int chunkBufferRadius = 2; // Agora não é final, pode ser ajustado

    /** Atomic reference to the BitSet representing which chunks should be rendered. */
    private static final AtomicReference<BitSet> chunksToRender = new AtomicReference<>(new BitSet());

    /** As coordenadas X do chunk mínimo no cálculo da frustum. */
    private static int minRenderChunkX = 0;
    /** As coordenadas Z do chunk mínimo no cálculo da frustum. */
    private static int minRenderChunkZ = 0;
    /** O tamanho da grade de chunks calculada (renderDistanceChunks * 2 + 1). */
    private static int renderGridSize = 0;

    /** A distância de renderização configurada pelo cliente. */
    private static int currentRenderDistanceChunks = 0;


    /** A última posição registrada do jogador. */
    private Vec3d lastPlayerPos = Vec3d.ZERO;

    /** O último yaw registrado do jogador. */
    private float lastPlayerYaw = 0f;

    /** O último pitch registrado do jogador. */
    private float lastPlayerPitch = 0f;

    /** O último vetor de movimento do jogador. */
    private Vec3d lastPlayerMovement = Vec3d.ZERO; // Para calcular a velocidade

    /** O limite de distância quadrada para recálculo (pode ser adaptativo). */
    private static double positionThresholdSquared = 1.0 * 1.0; // Agora não é final
    /** O limite de rotação para recálculo (pode ser adaptativo). */
    private static float rotationThreshold = 5.0f; // Agora não é final

    // Limites para ajuste adaptativo
    private static final double BASE_POSITION_THRESHOLD_SQUARED = 1.0 * 1.0;
    private static final float BASE_ROTATION_THRESHOLD = 5.0f;
    private static final int BASE_CHUNK_BUFFER_RADIUS = 2;
    private static final int FAST_MOVEMENT_CHUNK_BUFFER_RADIUS = 4; // Buffer maior para movimento rápido

    /**
     * Determina se os chunks a serem renderizados devem ser recalculados com base no movimento do jogador.
     * @param playerPos A posição atual do jogador.
     * @param yaw O yaw atual do jogador.
     * @param pitch O pitch atual do jogador.
     * @return true se o recálculo for necessário, false caso contrário.
     */
    public boolean shouldRecalculate(Vec3d playerPos, float yaw, float pitch) {
        // Calcula a velocidade do jogador
        Vec3d currentMovement = playerPos.subtract(lastPlayerPos);
        double speedSquared = currentMovement.lengthSquared();

        if (BariumConfig.ENABLE_ADAPTIVE_CHUNK_OPTIMIZATION) {
            // Ajusta thresholds e buffer radius baseados na velocidade
            adjustOptimizationParameters(speedSquared);
        } else {
            // Reseta para valores base se a otimização adaptativa estiver desativada
            resetOptimizationParameters();
        }

        boolean needsRecalculation = false;
        if (playerPos.squaredDistanceTo(lastPlayerPos) >= positionThresholdSquared) {
            needsRecalculation = true;
        }
        if (Math.abs(yaw - lastPlayerYaw) >= rotationThreshold || Math.abs(pitch - lastPlayerPitch) >= rotationThreshold) {
            needsRecalculation = true;
        }

        lastPlayerPos = playerPos;
        lastPlayerYaw = yaw;
        lastPlayerPitch = pitch;
        lastPlayerMovement = currentMovement; // Atualiza o último movimento
        return needsRecalculation;
    }

    /**
     * Ajusta dinamicamente os parâmetros de otimização com base na velocidade do jogador.
     * @param speedSquared A velocidade quadrada do jogador.
     */
    private void adjustOptimizationParameters(double speedSquared) {
        // Exemplos de thresholds de velocidade (ajuste conforme necessário)
        double walkingSpeedSq = 0.05 * 0.05; // Velocidade de caminhada
        double runningSpeedSq = 0.1 * 0.1;   // Velocidade de corrida
        double flyingSpeedSq = 0.5 * 0.5;    // Velocidade de voo

        if (speedSquared > flyingSpeedSq) {
            // Movimento muito rápido (voo rápido, elytras)
            positionThresholdSquared = BASE_POSITION_THRESHOLD_SQUARED * 0.25; // Recalcula mais frequentemente
            rotationThreshold = BASE_ROTATION_THRESHOLD * 0.5f; // Recalcula mais frequentemente
            chunkBufferRadius = FAST_MOVEMENT_CHUNK_BUFFER_RADIUS; // Aumenta o buffer
        } else if (speedSquared > runningSpeedSq) {
            // Movimento rápido (corrida)
            positionThresholdSquared = BASE_POSITION_THRESHOLD_SQUARED * 0.5;
            rotationThreshold = BASE_ROTATION_THRESHOLD * 0.75f;
            chunkBufferRadius = BASE_CHUNK_BUFFER_RADIUS + 1; // Levemente aumenta o buffer
        } else if (speedSquared > walkingSpeedSq) {
            // Movimento normal (caminhada)
            positionThresholdSquared = BASE_POSITION_THRESHOLD_SQUARED;
            rotationThreshold = BASE_ROTATION_THRESHOLD;
            chunkBufferRadius = BASE_CHUNK_BUFFER_RADIUS;
        } else {
            // Parado ou movimento muito lento
            positionThresholdSquared = BASE_POSITION_THRESHOLD_SQUARED * 2; // Recalcula menos frequentemente
            rotationThreshold = BASE_ROTATION_THRESHOLD * 1.5f; // Recalcula menos frequentemente
            chunkBufferRadius = BASE_CHUNK_BUFFER_RADIUS;
        }
    }

    /**
     * Reseta os parâmetros de otimização para seus valores base.
     */
    private void resetOptimizationParameters() {
        positionThresholdSquared = BASE_POSITION_THRESHOLD_SQUARED;
        rotationThreshold = BASE_ROTATION_THRESHOLD;
        chunkBufferRadius = BASE_CHUNK_BUFFER_RADIUS;
    }


    /**
     * Calcula quais chunks devem ser renderizados com base na posição e visão do jogador.
     * @param client A instância do cliente Minecraft.
     * @param playerPos A posição atual do jogador.
     * @param yaw O yaw atual do jogador.
     * @param pitch O pitch atual do jogador.
     */
    public void calculateChunksToRender(MinecraftClient client, Vec3d playerPos, float yaw, float pitch) {
        BitSet newChunksToRender = new BitSet();

        currentRenderDistanceChunks = client.options.getViewDistance().getValue();
        // Atualiza o campo estático
        ChunkRenderManager.currentRenderDistanceChunks = currentRenderDistanceChunks;


        // Usar o FOV do cliente para calcular o ângulo de culling
        // O campo de visão (fov) é em graus, precisamos converter para radianos e pegar o cosseno da metade do ângulo
        double fovRadians = Math.toRadians(client.options.getFov().getValue() / 2.0);
        double maxAngleCos = Math.cos(fovRadians);


        Vec3d lookVec = client.player.getRotationVec(1.0F).normalize();
        

        ChunkPos playerChunkPos = client.player.getChunkPos();
        int playerChunkX = playerChunkPos.x;
        int playerChunkZ = playerChunkPos.z;

        minRenderChunkX = playerChunkX - currentRenderDistanceChunks;
        int maxChunkX = playerChunkX + currentRenderDistanceChunks;
        minRenderChunkZ = playerChunkZ - currentRenderDistanceChunks;
        int maxChunkZ = playerChunkZ + currentRenderDistanceChunks;

        renderGridSize = currentRenderDistanceChunks * 2 + 1; // Atualiza o campo estático

        double playerX = playerPos.x;
        double playerZ = playerPos.z;

        // Calcula chunks visíveis
        for (int chunkX = minRenderChunkX; chunkX <= maxChunkX; chunkX++) {
            double chunkCenterX = (chunkX << 4) + 8;
            double deltaX = chunkCenterX - playerX;
            for (int chunkZ = minRenderChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                double chunkCenterZ = (chunkZ << 4) + 8;
                double deltaZ = chunkCenterZ - playerZ;

                double distanceSquared = deltaX * deltaX + deltaZ * deltaZ;
                if (distanceSquared == 0) { // O jogador está no centro do chunk
                    int localX = chunkX - minRenderChunkX;
                    int localZ = chunkZ - minRenderChunkZ;
                    int index = localX + localZ * renderGridSize;
                    newChunksToRender.set(index);
                    continue;
                }

                double invDistance = 1.0 / Math.sqrt(distanceSquared);
                double toChunkX = deltaX * invDistance;
                double toChunkZ = deltaZ * invDistance;

                double dotProduct = lookVec.x * toChunkX + lookVec.z * toChunkZ;

                if (dotProduct >= maxAngleCos) {
                    int localX = chunkX - minRenderChunkX;
                    int localZ = chunkZ - minRenderChunkZ;
                    int index = localX + localZ * renderGridSize;
                    newChunksToRender.set(index);
                }
            }
        }

        // Adiciona chunks de buffer ao redor do jogador (usando o chunkBufferRadius adaptativo)
        for (int dx = -chunkBufferRadius; dx <= chunkBufferRadius; dx++) { // Usa o valor adaptativo
            int chunkX = playerChunkX + dx;
            for (int dz = -chunkBufferRadius; dz <= chunkBufferRadius; dz++) { // Usa o valor adaptativo
                int chunkZ = playerChunkZ + dz;
                
                // Mapeia coordenadas globais do chunk para coordenadas locais da grade de renderização
                int localX = chunkX - minRenderChunkX;
                int localZ = chunkZ - minRenderChunkZ;

                // Garante que o chunk de buffer esteja dentro dos limites da grade de renderização
                if (localX >= 0 && localX < renderGridSize && localZ >= 0 && localZ < renderGridSize) {
                    int index = localX + localZ * renderGridSize;
                    newChunksToRender.set(index);
                }
            }
        }

        chunksToRender.set(newChunksToRender);
    }

    /**
     * Obtém o BitSet atual representando quais chunks devem ser renderizados.
     * @return Um BitSet onde os bits setados indicam chunks que devem ser renderizados.
     */
    public static BitSet getChunksToRender() {
        return chunksToRender.get();
    }

    /**
     * Obtém a coordenada X mínima do chunk no cálculo da frustum.
     * Usado por Mixins para mapear índices do BitSet.
     */
    public static int getMinRenderChunkX() {
        return minRenderChunkX;
    }

    /**
     * Obtém a coordenada Z mínima do chunk no cálculo da frustum.
     * Usado por Mixins para mapear índices do BitSet.
     */
    public static int getMinRenderChunkZ() {
        return minRenderChunkZ;
    }

    /**
     * Obtém o tamanho da grade de renderização calculada (renderDistanceChunks * 2 + 1).
     * Usado por Mixins para mapear índices do BitSet.
     */
    public static int getRenderGridSize() {
        return renderGridSize;
    }

    /**
     * Obtém a distância de renderização atual em chunks.
     * Usado por Mixins para determinar o contexto da grade.
     */
    public static int getCurrentRenderDistanceChunks() {
        return currentRenderDistanceChunks;
    }
}