package com.barium.client.optimization;

import com.barium.BariumMod;
import com.barium.config.BariumConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.chunk.ChunkBuilder;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.chunk.WorldChunk;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Otimizador de Terrain Streaming / Paging no lado do cliente.
 * 
 * Implementa:
 * - Descarte dinâmico de chunks não visíveis (culling de renderização)
 * - Pré-carregamento/priorização inteligente com base na direção de movimento (prioridade de renderização)
 * - Chunk Paging com LOD (simplificação de renderização)
 */
public class ClientTerrainOptimizer {
    // Cache para o nível de LOD de cada chunk renderizado
    private static final Map<ChunkPos, Integer> CHUNK_LOD_LEVELS = new HashMap<>();
    
    // Contadores para controlar a frequência de re-meshing/updates de chunks
    private static final Map<ChunkPos, Integer> CHUNK_UPDATE_COUNTERS = new HashMap<>();

    public static void init() {
        BariumMod.LOGGER.info("Inicializando otimizações de terrain streaming e LOD do lado do cliente");
    }

    /**
     * Determina se um chunk deve ser renderizado neste frame.
     * Incorpora culling baseado em distância e, futuramente, oclusão.
     * 
     * @param chunk O WorldChunk a ser avaliado.
     * @param camera A câmera do jogador.
     * @return true se o chunk deve ser renderizado, false caso contrário.
     */
    public static boolean shouldRenderChunk(WorldChunk chunk, Camera camera) {
        if (!BariumConfig.ENABLE_TERRAIN_STREAMING) {
            return true;
        }

        ChunkPos chunkPos = chunk.getPos();
        Vec3d chunkCenter = new Vec3d(chunkPos.getCenterX(), chunk.getHeight() / 2.0, chunkPos.getCenterZ());
        Vec3d cameraPos = camera.getPos();

        double distance = cameraPos.distanceTo(chunkCenter);

        // Culling simples baseado na distância do view distance (redundante se o Minecraft já faz isso, mas bom ter)
        if (distance > MinecraftClient.getInstance().options.getViewDistance().getValue() * 16 + 32) { // Adiciona um buffer
            return false;
        }

        // Frustum culling (Minecraft já faz isso, mas podemos otimizar chamadas caras se soubermos que está fora)
        // O mixin no WorldRenderer vai aplicar isso.
        
        // TODO: Implementar um culling de oclusão mais sofisticado (e.g., raycasting simplificado ou baseado em dados de altura)
        // Isso é complexo e pode ser adicionado em fases futuras. Por agora, o culling primário é baseado na distância.

        return true;
    }

    /**
     * Determina o nível de detalhe (LOD) para um chunk.
     * 
     * @param chunk O WorldChunk.
     * @param camera A câmera do jogador.
     * @return O nível de LOD (0 = máximo, 1 = médio, 2 = baixo, etc.).
     */
    public static int getChunkLOD(WorldChunk chunk, Camera camera) {
        if (!BariumConfig.ENABLE_CHUNK_LOD) {
            return 0; // Sempre LOD máximo
        }

        ChunkPos chunkPos = chunk.getPos();
        Vec3d chunkCenter = new Vec3d(chunkPos.getCenterX(), chunk.getHeight() / 2.0, chunkPos.getCenterZ());
        Vec3d cameraPos = camera.getPos();

        double distance = cameraPos.distanceTo(chunkCenter);

        if (distance < BariumConfig.CHUNK_LOD_DISTANCE_LEVEL1) {
            return 0; // Full detail
        } else if (distance < BariumConfig.CHUNK_LOD_DISTANCE_LEVEL2) {
            return 1; // Medium detail (e.g., skip complex blocks like block entities)
        } else {
            return 2; // Low detail (e.g., skip fluids, particles, render only solid geometry)
        }
    }

    /**
     * Verifica se um chunk deve ser re-meshed neste tick baseado no LOD e na direção do jogador.
     * Usado para otimizar a frequência de atualização das malhas dos chunks.
     * 
     * @param chunkPos A posição do chunk.
     * @param currentLOD O nível de LOD atual.
     * @param player O jogador atual.
     * @return true se o chunk deve ser re-meshed, false caso contrário.
     */
    public static boolean shouldRebuildChunkMesh(ChunkPos chunkPos, int currentLOD, PlayerEntity player) {
        if (!BariumConfig.ENABLE_CHUNK_LOD && !BariumConfig.ENABLE_DIRECTIONAL_PRELOADING) {
            return true; // Sem otimização, sempre rebuild
        }

        int updateInterval = 1; // Default
        if (currentLOD == 1) {
            updateInterval = BariumConfig.CHUNK_UPDATE_INTERVAL_LOD1;
        } else if (currentLOD == 2) {
            updateInterval = BariumConfig.CHUNK_UPDATE_INTERVAL_LOD2;
        }

        // Implementa pré-carregamento/priorização inteligente baseada na direção
        if (BariumConfig.ENABLE_DIRECTIONAL_PRELOADING && player != null && player.getVelocity().lengthSquared() > 0.01) {
            Vec3d movementDirection = player.getVelocity().normalize();
            Vec3d chunkCenter = new Vec3d(chunkPos.getCenterX(), player.getY(), chunkPos.getCenterZ()); // Simplificado para y do jogador
            Vec3d toChunk = chunkCenter.subtract(player.getPos()).normalize();
            double alignment = movementDirection.dotProduct(toChunk);

            if (alignment > BariumConfig.MOVEMENT_ALIGNMENT_THRESHOLD) {
                // Chunk está na frente, prioriza atualização (reduz intervalo)
                updateInterval = Math.max(1, updateInterval / 2); // Atualiza duas vezes mais rápido
            } else if (alignment < -BariumConfig.MOVEMENT_ALIGNMENT_THRESHOLD / 2.0) {
                // Chunk está atrás, desprioriza atualização (aumenta intervalo)
                updateInterval *= 2;
            }
        }

        int counter = CHUNK_UPDATE_COUNTERS.getOrDefault(chunkPos, 0) + 1;
        CHUNK_UPDATE_COUNTERS.put(chunkPos, counter);

        boolean timeToUpdate = (counter % updateInterval == 0);
        if (timeToUpdate) {
            CHUNK_UPDATE_COUNTERS.put(chunkPos, 0); // Resetar contador
        }

        return timeToUpdate;
    }

    /**
     * Limpa o cache de LOD e contadores de atualização de chunks.
     * Deve ser chamado ao carregar um novo mundo ou em situações de reinício.
     */
    public static void clearCaches() {
        CHUNK_LOD_LEVELS.clear();
        CHUNK_UPDATE_COUNTERS.clear();
        BariumMod.LOGGER.info("Caches de terreno do cliente limpos.");
    }
}