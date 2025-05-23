package com.barium.config;

/**
 * Configuração central para o mod Barium.
 * Contém todas as configurações para os diferentes sistemas de otimização.
 */
public class BariumConfig {
    // General Mod Settings (new)
    public static final boolean ENABLE_MOD_OPTIMIZATIONS = true; // Master switch for all Barium optimizations
    public static final boolean ENABLE_DEBUG_LOGGING = false; // Enable/disable verbose debug logging

    // Configurações para PathfindingOptimizer
    public static final int PATH_CACHE_SIZE = 128;
    public static final int PATH_UPDATE_INTERVAL_TICKS = 10;
    public static final int PATH_UPDATE_DISTANCE = 32;
    
    // Configurações para BlockTickOptimizer
    public static final boolean ENABLE_ON_DEMAND_TICKING = true;
    public static final int HOPPER_OPTIMIZATION_LEVEL = 2;
    
    // Configurações para RedstoneOptimizer
    public static final boolean ENABLE_SIGNAL_COMPRESSION = true;
    public static final int MAX_REDSTONE_UPDATES_PER_TICK = 1024;
    
    // Configurações para ParticleOptimizer (UPDATED/ADDED)
    public static final boolean ENABLE_PARTICLE_CULLING = true;
    public static final boolean ENABLE_PARTICLE_LOD = true;
    public static final boolean ENABLE_PARTICLE_OPTIMIZATIONS = true; // General particle optimizations switch
    public static final double PARTICLE_LOD_DISTANCE_START = 32.0; // Distance where LOD starts
    public static final double PARTICLE_LOD_STEP_DISTANCE = 16.0; // Distance interval for next LOD level
    public static final int MAX_PARTICLE_LOD_LEVELS = 3; // Max LOD levels
    public static final double PARTICLE_CULLING_DISTANCE_SQ = 64.0 * 64.0; // Squared distance for culling (64 blocks)
    public static final int MAX_TOTAL_PARTICLES = 10000; // Limit total particles rendered/ticked

    // Configurações para EntityTickOptimizer
    public static final int ENTITY_FREEZE_DISTANCE = 48;
    
    // Configurações para InventoryOptimizer
    public static final boolean ENABLE_SLOT_CACHING = true;
    
    // Configurações para ChunkSavingOptimizer
    public static final int CHUNK_SAVE_BUFFER_SIZE = 64;
    public static final boolean ENABLE_ASYNC_COMPRESSION = true;
    
    // Configurações para SoundOptimizer
    public static final boolean ENABLE_SOUND_CULLING = true;
    public static final int SOUND_CULLING_DISTANCE = 32;
    
    // Configurações para HudOptimizer
    public static final boolean ENABLE_HUD_CACHING = true;
    public static final boolean ENABLE_FONT_CACHING = true;
    public static final int HUD_UPDATE_INTERVAL_TICKS = 5;

    // NOVAS Configurações para ClientTerrainOptimizer
    public static final boolean ENABLE_TERRAIN_STREAMING = true;
    public static final boolean ENABLE_DIRECTIONAL_PRELOADING = true;
    public static final boolean ENABLE_CHUNK_LOD = true;
    public static final int CHUNK_LOD_DISTANCE_LEVEL1 = 48; // Distância para LOD level 1 (e.g., skip complex blocks)
    public static final int CHUNK_LOD_DISTANCE_LEVEL2 = 96; // Distância para LOD level 2 (e.g., skip fluids/entities)
    public static final int CHUNK_UPDATE_INTERVAL_LOD1 = 5; // Re-meshing a cada 5 ticks
    public static final int CHUNK_UPDATE_INTERVAL_LOD2 = 10; // Re-meshing a cada 10 ticks
    public static final double MOVEMENT_ALIGNMENT_THRESHOLD = 0.6; // Alinhamento para considerar "na frente" do jogador
}