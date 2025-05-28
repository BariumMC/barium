package com.barium.config;

/**
 * Configuração central para o mod Barium.
 * Contém todas as configurações para os diferentes sistemas de otimização.
 * Corrigido: Adicionadas/Renomeadas flags ausentes/incorretas para corrigir erros de compilação.
 */
public class BariumConfig {

    // --- Geral --- //
    // (Adicione flags gerais se necessário)

    // --- PathfindingOptimizer --- //
    public static final boolean ENABLE_PATHFINDING_OPTIMIZATION = true;
    public static final boolean USE_SMART_CACHE = true;
    public static final boolean SIMPLIFY_COLLISION = true;
    public static final boolean REDUCE_OFFSCREEN_PATHFINDING = true;
    public static final int PATH_CACHE_SIZE = 128;
    public static final int PATH_UPDATE_INTERVAL_TICKS = 10;
    public static final int PATH_UPDATE_DISTANCE = 32;

    // --- BlockTickOptimizer --- //
    public static final boolean ENABLE_BLOCK_TICK_OPTIMIZATION = true;
    public static final boolean USE_ON_DEMAND_TICKING = true;
    public static final boolean OPTIMIZE_HOPPERS = true;
    public static final int HOPPER_OPTIMIZATION_LEVEL = 2; // Exemplo, pode ser usado em shouldSkipHopperTick

    // --- RedstoneOptimizer --- //
    public static final boolean ENABLE_REDSTONE_OPTIMIZATION = true;
    public static final boolean LIMIT_SIGNAL_PROPAGATION = true;
    public static final boolean USE_COMPACT_QUEUE = true;
    public static final int MAX_REDSTONE_UPDATES_PER_TICK = 1024;

    // --- ParticleOptimizer (Client-side) --- //
    public static final boolean ENABLE_PARTICLE_OPTIMIZATION = true; // Renomeado de ENABLE_PARTICLE_CULLING para consistência
    public static final boolean ENABLE_PARTICLE_LOD = true;
    public static final int PARTICLE_LOD_DISTANCE = 32;

    // --- EntityTickOptimizer --- //
    public static final boolean ENABLE_ENTITY_TICK_OPTIMIZATION = true;
    public static final boolean FREEZE_FAR_ENTITIES = true;
    public static final boolean REDUCE_FAR_ENTITY_TICKS = true;
    public static final int ENTITY_FREEZE_DISTANCE = 48; // Distância para congelar completamente
    public static final int ENTITY_REDUCED_TICK_DISTANCE = 64; // Distância para reduzir frequência de ticks

    // --- InventoryOptimizer --- //
    public static final boolean ENABLE_INVENTORY_OPTIMIZATION = true;
    public static final boolean CACHE_EMPTY_SLOTS = true;

    // --- ChunkSavingOptimizer --- //
    public static final boolean ENABLE_CHUNK_SAVING_OPTIMIZATION = true;
    public static final boolean BUFFER_CHUNK_SAVES = true;
    public static final boolean ASYNC_CHUNK_COMPRESSION = true;
    public static final int CHUNK_SAVE_BUFFER_SIZE = 64;

    // --- SoundOptimizer (Client-side) --- //
    public static final boolean ENABLE_SOUND_OPTIMIZATION = true; // Renomeado de ENABLE_SOUND_CULLING
    public static final boolean ENABLE_SOUND_OBSTRUCTION_CHECK = true; // Adicionado
    public static final int SOUND_CULLING_DISTANCE = 32;

    // --- HudOptimizer (Client-side) --- //
    public static final boolean ENABLE_HUD_OPTIMIZATION = true; // Renomeado de ENABLE_HUD_CACHING
    public static final boolean CACHE_DEBUG_HUD = true; // Adicionado (usado em HudOptimizer)
    public static final boolean REDUCE_HUD_UPDATES = true; // Adicionado (usado em HudOptimizer)
    public static final boolean ENABLE_FONT_CACHING = true;
    public static final int HUD_UPDATE_INTERVAL_TICKS = 5;

    // --- Advanced Client Optimizations --- //
    // Flag geral para otimizações avançadas (usada em alguns otimizadores)
    public static final boolean ENABLE_ADVANCED_CULLING = true; // Adicionado para consistência
    
    public static final boolean ENABLE_ADVANCED_OCCLUSION_CULLING = true;
    public static final boolean SHOW_DEBUG_OVERLAY = false; // Adicionado para AdvancedOcclusionCulling
    
    // --- TransparentBlockOptimizer (Client-side) --- //
    public static final boolean ENABLE_TRANSPARENT_BLOCK_OPTIMIZATION = true;
    public static final boolean OPTIMIZE_TRANSPARENT_SORTING = true; // Adicionado para TransparentBlockOptimizer
    public static final boolean USE_TRANSPARENT_INSTANCING = true; // Adicionado para TransparentBlockOptimizer
    
    // --- TileEntityOptimizer (Client-side) --- //
    public static final boolean ENABLE_TILE_ENTITY_RENDERING_OPTIMIZATION = true;
    public static final boolean OPTIMIZE_TILE_ENTITIES = true; // Adicionado para TileEntityOptimizer
    public static final boolean USE_TILE_ENTITY_INSTANCING = true; // Adicionado para TileEntityOptimizer
    
    // --- AnimationCullingOptimizer (Client-side) --- //
    public static final boolean ENABLE_ANIMATION_CULLING = true;
    
    // --- ChunkMeshPriorityOptimizer (Client-side) --- //
    public static final boolean ENABLE_CHUNK_MESH_PRIORITY_OPTIMIZATION = true;
    public static final boolean ADAPTIVE_CHUNK_PRIORITY = true; // Adicionado para ChunkMeshPriorityOptimizer

    // --- Chunk Occlusion (Client-side) --- //
    public static final boolean ENABLE_CHUNK_OCCLUSION_OPTIMIZATION = true; // Adicionado
}
