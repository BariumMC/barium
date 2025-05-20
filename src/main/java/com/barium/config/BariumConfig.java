package com.barium.config;

/**
 * Configuração central para o mod Barium.
 * Contém todas as configurações para os diferentes sistemas de otimização.
 */
public class BariumConfig {
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
    
    // Configurações para ParticleOptimizer
    public static final boolean ENABLE_PARTICLE_CULLING = true;
    public static final boolean ENABLE_PARTICLE_LOD = true;
    public static final int PARTICLE_LOD_DISTANCE = 32;
    
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
}
