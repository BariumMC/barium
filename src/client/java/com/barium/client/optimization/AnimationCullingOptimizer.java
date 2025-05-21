package com.barium.client.optimization;

import com.barium.BariumMod;
import com.barium.config.BariumConfig;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.chunk.ChunkBuilder;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.chunk.ChunkSection;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Otimizador de animações de blocos.
 * 
 * Implementa:
 * - Culling de animações para blocos fora do campo de visão
 * - Redução da taxa de animação para blocos distantes
 * - Pausa de animações para chunks não visíveis
 */
public class AnimationCullingOptimizer {
    // Cache de blocos animados por chunk
    private static final Map<ChunkPos, Set<BlockPos>> ANIMATED_BLOCKS_CACHE = new ConcurrentHashMap<>();
    
    // Cache de estado de animação por bloco
    private static final Map<BlockPos, AnimationState> ANIMATION_STATES = new ConcurrentHashMap<>();
    
    // Timestamp da última atualização do cache por chunk
    private static final Map<ChunkPos, Long> LAST_CACHE_UPDATE = new ConcurrentHashMap<>();
    
    // Tempo de validade do cache em milissegundos
    private static final long CACHE_VALIDITY_TIME = 2000;
    
    // Enum para estados de animação
    public enum AnimationState {
        FULL_SPEED,    // Animação em velocidade normal
        HALF_SPEED,    // Animação em metade da velocidade
        QUARTER_SPEED, // Animação em um quarto da velocidade
        PAUSED         // Animação pausada
    }
    
    public static void init() {
        BariumMod.LOGGER.info("Inicializando otimizações de culling de animações");
    }
    
    /**
     * Verifica se um bloco deve ter sua animação atualizada neste tick
     * 
     * @param pos Posição do bloco
     * @param state Estado do bloco
     * @param tickCounter Contador de ticks atual
     * @return true se a animação deve ser atualizada, false caso contrário
     */
    public static boolean shouldUpdateAnimation(BlockPos pos, BlockState state, int tickCounter) {
        if (!BariumConfig.ENABLE_ANIMATION_CULLING) {
            return true; // Sem otimização, atualiza normalmente
        }
        
        // Verifica se o bloco tem animação
        if (!hasAnimation(state)) {
            return false; // Bloco sem animação
        }
        
        // Obtém o estado de animação atual
        AnimationState animState = getAnimationState(pos);
        
        // Decide se deve atualizar com base no estado e no contador de ticks
        switch (animState) {
            case FULL_SPEED:
                return true; // Atualiza em todos os ticks
            case HALF_SPEED:
                return tickCounter % 2 == 0; // Atualiza a cada 2 ticks
            case QUARTER_SPEED:
                return tickCounter % 4 == 0; // Atualiza a cada 4 ticks
            case PAUSED:
                return false; // Não atualiza
            default:
                return true;
        }
    }
    
    /**
     * Verifica se um bloco tem animação
     * 
     * @param state Estado do bloco
     * @return true se o bloco tem animação, false caso contrário
     */
    private static boolean hasAnimation(BlockState state) {
        // Lista de blocos que tipicamente têm animações
        Block block = state.getBlock();
        String blockName = block.getTranslationKey().toLowerCase();
        
        return blockName.contains("water") ||
               blockName.contains("lava") ||
               blockName.contains("fire") ||
               blockName.contains("portal") ||
               blockName.contains("redstone") ||
               blockName.contains("piston") ||
               blockName.contains("lantern") ||
               blockName.contains("campfire") ||
               blockName.contains("sea_pickle") ||
               blockName.contains("conduit") ||
               blockName.contains("beacon");
    }
    
    /**
     * Obtém o estado de animação para um bloco
     * 
     * @param pos Posição do bloco
     * @return Estado de animação
     */
    public static AnimationState getAnimationState(BlockPos pos) {
        // Verifica se já temos um estado em cache
        AnimationState cachedState = ANIMATION_STATES.get(pos);
        if (cachedState != null) {
            return cachedState;
        }
        
        // Calcula o estado com base na visibilidade e distância
        AnimationState state = calculateAnimationState(pos);
        
        // Armazena no cache
        ANIMATION_STATES.put(pos, state);
        
        return state;
    }
    
    /**
     * Calcula o estado de animação para um bloco com base na visibilidade e distância
     * 
     * @param pos Posição do bloco
     * @return Estado de animação calculado
     */
    private static AnimationState calculateAnimationState(BlockPos pos) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null || client.player == null) {
            return AnimationState.FULL_SPEED; // Sem mundo ou jogador, usa velocidade normal
        }
        
        // Verifica se o chunk está carregado
        ChunkPos chunkPos = new ChunkPos(pos);
        if (!client.world.getChunkManager().isChunkLoaded(chunkPos.x, chunkPos.z)) {
            return AnimationState.PAUSED; // Chunk não carregado, pausa a animação
        }
        
        // Verifica se o bloco está no campo de visão
        if (!isInFrustum(pos)) {
            return AnimationState.PAUSED; // Fora do campo de visão, pausa a animação
        }
        
        // Calcula a distância ao jogador
        double distance = client.player.squaredDistanceTo(
                pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
        
        // Define o estado com base na distância
        if (distance < 16 * 16) { // Menos de 16 blocos
            return AnimationState.FULL_SPEED;
        } else if (distance < 32 * 32) { // Entre 16 e 32 blocos
            return AnimationState.HALF_SPEED;
        } else if (distance < 64 * 64) { // Entre 32 e 64 blocos
            return AnimationState.QUARTER_SPEED;
        } else { // Mais de 64 blocos
            return AnimationState.PAUSED;
        }
    }
    
    /**
     * Verifica se um bloco está dentro do frustum da câmera
     * 
     * @param pos Posição do bloco
     * @return true se está no frustum, false caso contrário
     */
    private static boolean isInFrustum(BlockPos pos) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.gameRenderer == null || client.gameRenderer.getCamera() == null) {
            return true; // Sem câmera, considera visível
        }
        
        // Posição do bloco
        Vec3d blockPos = new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
        
        // Posição da câmera
        Vec3d cameraPos = client.gameRenderer.getCamera().getPos();
        
        // Direção da câmera
        float pitch = client.gameRenderer.getCamera().getPitch();
        float yaw = client.gameRenderer.getCamera().getYaw();
        Vec3d lookVec = Vec3d.fromPolar(pitch, yaw);
        
        // Vetor da câmera para o bloco
        Vec3d toBlock = blockPos.subtract(cameraPos).normalize();
        
        // Produto escalar para verificar se está no campo de visão
        double dot = toBlock.dotProduct(lookVec);
        
        // Considera visível se estiver dentro de um cone de ~120 graus
        return dot > -0.5;
    }
    
    /**
     * Encontra todos os blocos animados em um chunk
     * 
     * @param chunkPos Posição do chunk
     * @return Conjunto de posições de blocos animados
     */
    public static Set<BlockPos> findAnimatedBlocks(ChunkPos chunkPos) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) {
            return Collections.emptySet(); // Sem mundo, retorna conjunto vazio
        }
        
        // Verifica se o cache é válido
        long currentTime = System.currentTimeMillis();
        Long lastUpdate = LAST_CACHE_UPDATE.get(chunkPos);
        if (lastUpdate != null && currentTime - lastUpdate < CACHE_VALIDITY_TIME) {
            // Cache válido, retorna o conjunto armazenado
            Set<BlockPos> cachedBlocks = ANIMATED_BLOCKS_CACHE.get(chunkPos);
            return cachedBlocks != null ? cachedBlocks : Collections.emptySet();
        }
        
        // Verifica se o chunk está carregado
        if (!client.world.getChunkManager().isChunkLoaded(chunkPos.x, chunkPos.z)) {
            return Collections.emptySet(); // Chunk não carregado
        }
        
        // Conjunto para armazenar os blocos animados
        Set<BlockPos> animatedBlocks = new HashSet<>();
        
        // Percorre todas as seções do chunk
        for (int y = client.world.getBottomSectionCoord(); y < client.world.getTopSectionCoord(); y++) {
            ChunkSectionPos sectionPos = ChunkSectionPos.from(chunkPos, y);
            ChunkSection section = client.world.getChunk(chunkPos.x, chunkPos.z)
                    .getSectionArray()[y - client.world.getBottomSectionCoord()];
            
            if (section == null || section.isEmpty()) {
                continue; // Seção vazia
            }
            
            // Percorre todos os blocos na seção
            BlockPos.Mutable mutablePos = new BlockPos.Mutable();
            for (int sx = 0; sx < 16; sx++) {
                for (int sy = 0; sy < 16; sy++) {
                    for (int sz = 0; sz < 16; sz++) {
                        // Calcula a posição global do bloco
                        mutablePos.set(
                                chunkPos.x * 16 + sx,
                                sectionPos.getY() * 16 + sy,
                                chunkPos.z * 16 + sz
                        );
                        
                        // Obtém o estado do bloco
                        BlockState state = client.world.getBlockState(mutablePos);
                        
                        // Verifica se o bloco tem animação
                        if (hasAnimation(state)) {
                            animatedBlocks.add(mutablePos.toImmutable());
                        }
                    }
                }
            }
        }
        
        // Atualiza o cache
        ANIMATED_BLOCKS_CACHE.put(chunkPos, animatedBlocks);
        LAST_CACHE_UPDATE.put(chunkPos, currentTime);
        
        return animatedBlocks;
    }
    
    /**
     * Atualiza os estados de animação para todos os blocos em um chunk
     * 
     * @param chunkPos Posição do chunk
     */
    public static void updateAnimationStates(ChunkPos chunkPos) {
        // Obtém os blocos animados no chunk
        Set<BlockPos> animatedBlocks = findAnimatedBlocks(chunkPos);
        
        // Atualiza o estado de cada bloco
        for (BlockPos pos : animatedBlocks) {
            AnimationState state = calculateAnimationState(pos);
            ANIMATION_STATES.put(pos, state);
        }
    }
    
    /**
     * Limpa os caches de animação
     */
    public static void clearCaches() {
        ANIMATED_BLOCKS_CACHE.clear();
        ANIMATION_STATES.clear();
        LAST_CACHE_UPDATE.clear();
    }
    
    /**
     * Invalida o cache para um chunk específico
     * 
     * @param chunkPos Posição do chunk
     */
    public static void invalidateCache(ChunkPos chunkPos) {
        ANIMATED_BLOCKS_CACHE.remove(chunkPos);
        LAST_CACHE_UPDATE.remove(chunkPos);
        
        // Remove os estados de animação para os blocos neste chunk
        ANIMATION_STATES.entrySet().removeIf(entry -> {
            BlockPos pos = entry.getKey();
            ChunkPos posChunk = new ChunkPos(pos);
            return posChunk.equals(chunkPos);
        });
    }
}
