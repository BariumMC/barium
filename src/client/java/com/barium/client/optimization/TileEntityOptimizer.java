package com.barium.client.optimization;

import com.barium.BariumMod;
import com.barium.config.BariumConfig;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Otimizador de renderização de Tile Entities dinâmicas.
 * 
 * Implementa:
 * - Instancing para tile entities similares
 * - Culling de tile entities fora do campo de visão
 * - Otimização de renderização por tipo
 */
public class TileEntityOptimizer {
    // Cache de tile entities por tipo
    private static final Map<Class<? extends BlockEntity>, List<BlockEntity>> TILE_ENTITIES_BY_TYPE = new ConcurrentHashMap<>();
    
    // Cache de modelos para instancing
    private static final Map<Class<? extends BlockEntity>, Object> MODEL_CACHE = new ConcurrentHashMap<>();
    
    // Timestamp da última atualização do cache
    private static long lastCacheUpdate = 0;
    
    // Tempo de validade do cache em milissegundos
    private static final long CACHE_VALIDITY_TIME = 500;
    
    public static void init() {
        BariumMod.LOGGER.info("Inicializando otimizações de renderização de Tile Entities dinâmicas");
    }
    
    /**
     * Prepara as tile entities para renderização otimizada
     * 
     * @param tileEntities Lista de tile entities a renderizar
     * @return Mapa de tile entities agrupadas por tipo
     */
    public static Map<Class<? extends BlockEntity>, List<BlockEntity>> prepareForRendering(Collection<BlockEntity> tileEntities) {
        if (!BariumConfig.ENABLE_TILE_ENTITY_OPTIMIZATION) {
            return null; // Sem otimização, usa o sistema vanilla
        }
        
        // Verifica se o cache é válido
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastCacheUpdate < CACHE_VALIDITY_TIME && !TILE_ENTITIES_BY_TYPE.isEmpty()) {
            // Cache válido, retorna o agrupamento armazenado
            return TILE_ENTITIES_BY_TYPE;
        }
        
        // Limpa o cache antigo
        TILE_ENTITIES_BY_TYPE.clear();
        
        // Filtra e agrupa as tile entities por tipo
        for (BlockEntity entity : tileEntities) {
            // Ignora entidades nulas
            if (entity == null) {
                continue;
            }
            
            // Verifica se a tile entity está no campo de visão
            if (!isInFrustum(entity)) {
                continue;
            }
            
            // Agrupa por tipo
            Class<? extends BlockEntity> entityClass = entity.getClass();
            TILE_ENTITIES_BY_TYPE.computeIfAbsent(entityClass, k -> new ArrayList<>()).add(entity);
        }
        
        // Atualiza o timestamp do cache
        lastCacheUpdate = currentTime;
        
        return TILE_ENTITIES_BY_TYPE;
    }
    
    /**
     * Renderiza um grupo de tile entities do mesmo tipo usando instancing quando possível
     * 
     * @param entityClass O tipo de tile entity
     * @param entities Lista de tile entities do mesmo tipo
     * @param dispatcher O dispatcher de renderização
     * @param matrices A matriz de transformação
     * @param vertexConsumers O provedor de consumidores de vértices
     * @param light O nível de luz
     * @param overlay O overlay
     */
    public static void renderEntitiesByType(Class<? extends BlockEntity> entityClass, 
                                           List<BlockEntity> entities,
                                           BlockEntityRenderDispatcher dispatcher,
                                           MatrixStack matrices,
                                           VertexConsumerProvider vertexConsumers,
                                           int light,
                                           int overlay) {
        if (!BariumConfig.ENABLE_TILE_ENTITY_INSTANCING || entities.isEmpty()) {
            return;
        }
        
        // Verifica se este tipo suporta instancing
        if (supportsInstancing(entityClass)) {
            // Renderiza usando instancing
            renderWithInstancing(entityClass, entities, dispatcher, matrices, vertexConsumers, light, overlay);
        } else {
            // Renderiza individualmente, mas com otimizações
            renderIndividually(entities, dispatcher, matrices, vertexConsumers, light, overlay);
        }
    }
    
    /**
     * Verifica se um tipo de tile entity suporta renderização por instancing
     * 
     * @param entityClass O tipo de tile entity
     * @return true se suporta instancing, false caso contrário
     */
    private static boolean supportsInstancing(Class<? extends BlockEntity> entityClass) {
        // Lista de tipos que suportam instancing
        // Em um mod real, isso seria determinado por análise ou configuração
        return entityClass.getSimpleName().contains("Chest") ||
               entityClass.getSimpleName().contains("Shulker") ||
               entityClass.getSimpleName().contains("Banner") ||
               entityClass.getSimpleName().contains("Sign") ||
               entityClass.getSimpleName().contains("Bed");
    }
    
    /**
     * Renderiza um grupo de tile entities usando instancing
     * 
     * @param entityClass O tipo de tile entity
     * @param entities Lista de tile entities do mesmo tipo
     * @param dispatcher O dispatcher de renderização
     * @param matrices A matriz de transformação
     * @param vertexConsumers O provedor de consumidores de vértices
     * @param light O nível de luz
     * @param overlay O overlay
     */
    private static void renderWithInstancing(Class<? extends BlockEntity> entityClass,
                                            List<BlockEntity> entities,
                                            BlockEntityRenderDispatcher dispatcher,
                                            MatrixStack matrices,
                                            VertexConsumerProvider vertexConsumers,
                                            int light,
                                            int overlay) {
        // Obtém ou cria o modelo compartilhado para instancing
        Object sharedModel = getOrCreateSharedModel(entityClass);
        
        // Em um mod real, aqui seria implementado o instancing usando
        // batching de geometria ou instanced arrays do OpenGL
        
        // Para cada entidade
        for (BlockEntity entity : entities) {
            // Salva o estado da matriz
            matrices.push();
            
            // Posiciona a matriz na posição da entidade
            BlockPos pos = entity.getPos();
            matrices.translate(pos.getX() - dispatcher.camera.getPos().x,
                              pos.getY() - dispatcher.camera.getPos().y,
                              pos.getZ() - dispatcher.camera.getPos().z);
            
            // Renderiza a entidade usando o modelo compartilhado
            // Em um mod real, aqui seria adicionada a instância ao batch
            
            // Restaura o estado da matriz
            matrices.pop();
        }
        
        // Em um mod real, aqui seria renderizado o batch completo
    }
    
    /**
     * Renderiza tile entities individualmente, mas com otimizações
     * 
     * @param entities Lista de tile entities
     * @param dispatcher O dispatcher de renderização
     * @param matrices A matriz de transformação
     * @param vertexConsumers O provedor de consumidores de vértices
     * @param light O nível de luz
     * @param overlay O overlay
     */
    private static void renderIndividually(List<BlockEntity> entities,
                                          BlockEntityRenderDispatcher dispatcher,
                                          MatrixStack matrices,
                                          VertexConsumerProvider vertexConsumers,
                                          int light,
                                          int overlay) {
        // Ordena as entidades por distância para otimizar o Z-buffer
        entities.sort(Comparator.comparingDouble(entity -> 
                dispatcher.camera.getPos().squaredDistanceTo(
                        entity.getPos().getX() + 0.5,
                        entity.getPos().getY() + 0.5,
                        entity.getPos().getZ() + 0.5)));
        
        // Para cada entidade
        for (BlockEntity entity : entities) {
            // Obtém o renderizador específico para este tipo
            BlockEntityRenderer<BlockEntity> renderer = 
                    (BlockEntityRenderer<BlockEntity>) dispatcher.get(entity);
            
            if (renderer == null) {
                continue;
            }
            
            // Salva o estado da matriz
            matrices.push();
            
            // Posiciona a matriz na posição da entidade
            BlockPos pos = entity.getPos();
            matrices.translate(pos.getX() - dispatcher.camera.getPos().x,
                              pos.getY() - dispatcher.camera.getPos().y,
                              pos.getZ() - dispatcher.camera.getPos().z);
            
            // Renderiza a entidade
            renderer.render(entity, dispatcher.camera.getTickDelta(), 
                           matrices, vertexConsumers, light, overlay);
            
            // Restaura o estado da matriz
            matrices.pop();
        }
    }
    
    /**
     * Obtém ou cria um modelo compartilhado para instancing
     * 
     * @param entityClass O tipo de tile entity
     * @return O modelo compartilhado
     */
    private static Object getOrCreateSharedModel(Class<? extends BlockEntity> entityClass) {
        // Verifica se já temos um modelo em cache
        Object model = MODEL_CACHE.get(entityClass);
        if (model != null) {
            return model;
        }
        
        // Cria um novo modelo
        // Em um mod real, aqui seria criado um modelo otimizado para instancing
        model = new Object(); // Placeholder
        
        // Armazena no cache
        MODEL_CACHE.put(entityClass, model);
        
        return model;
    }
    
    /**
     * Verifica se uma tile entity está dentro do frustum da câmera
     * 
     * @param entity A tile entity
     * @return true se está no frustum, false caso contrário
     */
    private static boolean isInFrustum(BlockEntity entity) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.gameRenderer == null || client.gameRenderer.getCamera() == null) {
            return true; // Sem câmera, considera visível
        }
        
        // Posição da entidade
        BlockPos pos = entity.getPos();
        Vec3d entityPos = new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
        
        // Posição da câmera
        Vec3d cameraPos = client.gameRenderer.getCamera().getPos();
        
        // Direção da câmera
        float pitch = client.gameRenderer.getCamera().getPitch();
        float yaw = client.gameRenderer.getCamera().getYaw();
        Vec3d lookVec = Vec3d.fromPolar(pitch, yaw);
        
        // Vetor da câmera para a entidade
        Vec3d toEntity = entityPos.subtract(cameraPos).normalize();
        
        // Produto escalar para verificar se está no campo de visão
        double dot = toEntity.dotProduct(lookVec);
        
        // Considera visível se estiver dentro de um cone de ~120 graus
        return dot > -0.5;
    }
    
    /**
     * Limpa os caches de tile entities
     */
    public static void clearCaches() {
        TILE_ENTITIES_BY_TYPE.clear();
        MODEL_CACHE.clear();
        lastCacheUpdate = 0;
    }
}
