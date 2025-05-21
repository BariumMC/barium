package com.barium.client.optimization;

import com.barium.BariumMod;
import com.barium.config.BariumConfig;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.chunk.ChunkBuilder;
import net.minecraft.client.render.chunk.ChunkRendererRegion;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Otimizador de renderização de blocos transparentes.
 * 
 * Implementa:
 * - Algoritmo avançado de depth sorting para blocos transparentes
 * - Agrupamento de meshes transparentes por tipo
 * - Otimização de draw calls para blocos transparentes
 */
public class TransparentBlockOptimizer {
    // Cache de blocos transparentes por chunk
    private static final Map<ChunkBuilder.BuiltChunk, List<BlockPos>> TRANSPARENT_BLOCKS_CACHE = new ConcurrentHashMap<>();
    
    // Cache de ordem de renderização por chunk
    private static final Map<ChunkBuilder.BuiltChunk, List<BlockPos>> SORTED_RENDER_ORDER = new ConcurrentHashMap<>();
    
    // Tempo de validade do cache em milissegundos
    private static final long CACHE_VALIDITY_TIME = 1000;
    
    // Timestamp da última atualização do cache por chunk
    private static final Map<ChunkBuilder.BuiltChunk, Long> LAST_CACHE_UPDATE = new ConcurrentHashMap<>();
    
    public static void init() {
        BariumMod.LOGGER.info("Inicializando otimizações de renderização de blocos transparentes");
    }
    
    /**
     * Prepara os blocos transparentes para renderização em um chunk
     * 
     * @param chunk O chunk a ser renderizado
     * @param region A região do renderizador de chunk
     * @return Lista de blocos transparentes ordenados por profundidade
     */
    public static List<BlockPos> prepareTransparentBlocks(ChunkBuilder.BuiltChunk chunk, ChunkRendererRegion region) {
        if (!BariumConfig.ENABLE_TRANSPARENT_SORTING_OPTIMIZATION) {
            return null; // Sem otimização, usa o sistema vanilla
        }
        
        // Verifica se o cache é válido
        long currentTime = System.currentTimeMillis();
        Long lastUpdate = LAST_CACHE_UPDATE.get(chunk);
        if (lastUpdate != null && currentTime - lastUpdate < CACHE_VALIDITY_TIME) {
            // Cache válido, retorna a ordem de renderização armazenada
            return SORTED_RENDER_ORDER.get(chunk);
        }
        
        // Identifica todos os blocos transparentes no chunk
        List<BlockPos> transparentBlocks = findTransparentBlocks(chunk, region);
        TRANSPARENT_BLOCKS_CACHE.put(chunk, transparentBlocks);
        
        // Ordena os blocos por profundidade
        List<BlockPos> sortedBlocks = sortBlocksByDepth(transparentBlocks);
        SORTED_RENDER_ORDER.put(chunk, sortedBlocks);
        
        // Atualiza o timestamp do cache
        LAST_CACHE_UPDATE.put(chunk, currentTime);
        
        return sortedBlocks;
    }
    
    /**
     * Encontra todos os blocos transparentes em um chunk
     * 
     * @param chunk O chunk a ser analisado
     * @param region A região do renderizador de chunk
     * @return Lista de posições de blocos transparentes
     */
    private static List<BlockPos> findTransparentBlocks(ChunkBuilder.BuiltChunk chunk, ChunkRendererRegion region) {
        List<BlockPos> transparentBlocks = new ArrayList<>();
        
        // Obtém a origem do chunk
        BlockPos origin = chunk.getOrigin();
        
        // Percorre todos os blocos no chunk
        for (int x = 0; x < 16; x++) {
            for (int y = 0; y < 16; y++) {
                for (int z = 0; z < 16; z++) {
                    BlockPos pos = origin.add(x, y, z);
                    
                    // Verifica se o bloco está na região
                    if (!region.isInBounds(pos)) {
                        continue;
                    }
                    
                    // Obtém o estado do bloco
                    BlockState state = region.getBlockState(pos);
                    
                    // Verifica se o bloco é transparente
                    if (isTransparent(state)) {
                        transparentBlocks.add(pos);
                    }
                }
            }
        }
        
        return transparentBlocks;
    }
    
    /**
     * Verifica se um estado de bloco é transparente
     * 
     * @param state O estado do bloco
     * @return true se o bloco é transparente, false caso contrário
     */
    private static boolean isTransparent(BlockState state) {
        // Verifica se o bloco usa uma camada de renderização transparente
        RenderLayer layer = RenderLayer.getBlockLayer(state);
        return layer == RenderLayer.getTranslucent() || 
               layer == RenderLayer.getCutout() || 
               layer == RenderLayer.getCutoutMipped();
    }
    
    /**
     * Ordena blocos por profundidade em relação à câmera
     * 
     * @param blocks Lista de blocos a ordenar
     * @return Lista ordenada de blocos (do mais distante ao mais próximo)
     */
    private static List<BlockPos> sortBlocksByDepth(List<BlockPos> blocks) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.gameRenderer == null || client.gameRenderer.getCamera() == null) {
            return blocks; // Sem câmera, retorna a lista original
        }
        
        // Obtém a posição da câmera
        Vec3d cameraPos = client.gameRenderer.getCamera().getPos();
        
        // Cria uma cópia da lista para ordenação
        List<BlockPos> sortedBlocks = new ArrayList<>(blocks);
        
        // Ordena os blocos por distância à câmera (do mais distante ao mais próximo)
        sortedBlocks.sort((a, b) -> {
            double distA = distanceSquared(a, cameraPos);
            double distB = distanceSquared(b, cameraPos);
            return Double.compare(distB, distA); // Ordem decrescente
        });
        
        return sortedBlocks;
    }
    
    /**
     * Agrupa blocos transparentes por tipo para otimizar draw calls
     * 
     * @param blocks Lista de blocos transparentes
     * @param region A região do renderizador de chunk
     * @return Mapa de blocos agrupados por tipo
     */
    public static Map<BlockState, List<BlockPos>> groupTransparentBlocksByType(List<BlockPos> blocks, ChunkRendererRegion region) {
        Map<BlockState, List<BlockPos>> groupedBlocks = new HashMap<>();
        
        for (BlockPos pos : blocks) {
            // Verifica se o bloco está na região
            if (!region.isInBounds(pos)) {
                continue;
            }
            
            // Obtém o estado do bloco
            BlockState state = region.getBlockState(pos);
            
            // Agrupa por estado do bloco
            groupedBlocks.computeIfAbsent(state, k -> new ArrayList<>()).add(pos);
        }
        
        return groupedBlocks;
    }
    
    /**
     * Otimiza a ordem de renderização para minimizar trocas de estado
     * 
     * @param groupedBlocks Mapa de blocos agrupados por tipo
     * @return Lista otimizada de pares (estado, posição) para renderização
     */
    public static List<Map.Entry<BlockState, BlockPos>> optimizeRenderOrder(Map<BlockState, List<BlockPos>> groupedBlocks) {
        List<Map.Entry<BlockState, BlockPos>> renderOrder = new ArrayList<>();
        
        // Para cada tipo de bloco
        for (Map.Entry<BlockState, List<BlockPos>> entry : groupedBlocks.entrySet()) {
            BlockState state = entry.getKey();
            List<BlockPos> positions = entry.getValue();
            
            // Adiciona cada posição com seu estado
            for (BlockPos pos : positions) {
                renderOrder.add(new AbstractMap.SimpleEntry<>(state, pos));
            }
        }
        
        return renderOrder;
    }
    
    /**
     * Calcula o quadrado da distância entre um bloco e a câmera
     * 
     * @param pos Posição do bloco
     * @param cameraPos Posição da câmera
     * @return Quadrado da distância
     */
    private static double distanceSquared(BlockPos pos, Vec3d cameraPos) {
        double dx = pos.getX() + 0.5 - cameraPos.x;
        double dy = pos.getY() + 0.5 - cameraPos.y;
        double dz = pos.getZ() + 0.5 - cameraPos.z;
        return dx * dx + dy * dy + dz * dz;
    }
    
    /**
     * Limpa os caches de blocos transparentes
     */
    public static void clearCaches() {
        TRANSPARENT_BLOCKS_CACHE.clear();
        SORTED_RENDER_ORDER.clear();
        LAST_CACHE_UPDATE.clear();
    }
    
    /**
     * Invalida o cache para um chunk específico
     * 
     * @param chunk O chunk a invalidar
     */
    public static void invalidateCache(ChunkBuilder.BuiltChunk chunk) {
        TRANSPARENT_BLOCKS_CACHE.remove(chunk);
        SORTED_RENDER_ORDER.remove(chunk);
        LAST_CACHE_UPDATE.remove(chunk);
    }
}
