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
    public static final int HOPPER_OPTIMIZATION_LEVEL = 2; // 0=desabilitado, 1=básico, 2=avançado
    
    // Configurações para RedstoneOptimizer
    public static final boolean ENABLE_SIGNAL_COMPRESSION = true;
    public static final int MAX_REDSTONE_UPDATES_PER_TICK = 1024;
    
    // Configurações para ParticleOptimizer
    public static final boolean ENABLE_PARTICLE_CULLING = true;
    public static final double PARTICLE_CULLING_DISTANCE = 128.0; // Distância máxima para renderizar partículas
    public static final double PARTICLE_CULLING_FOV_THRESHOLD = 0.5; // Limite de produto escalar para FOV (0.5 ~ 60 graus de FOV)

    public static final boolean ENABLE_PARTICLE_LOD = true;
    public static final double PARTICLE_LOD_DISTANCE_START = 32.0; // Distância para começar a aplicar LOD
    public static final double PARTICLE_LOD_STEP_DISTANCE = 16.0; // A cada X blocos após PARTICLE_LOD_DISTANCE_START, aumenta o nível de LOD
    public static final int MAX_PARTICLE_LOD_LEVELS = 3; // Níveis de LOD máximos (0=máximo, 1, 2, 3)

    // Configurações para EntityTickOptimizer
    public static final int ENTITY_FREEZE_DISTANCE = 48; // Distância para entidades começarem a ter ticks reduzidos ou congelados
    
    // Configurações para InventoryOptimizer
    public static final boolean ENABLE_SLOT_CACHING = true;
    
    // Configurações para ChunkSavingOptimizer
    public static final int CHUNK_SAVE_BUFFER_SIZE = 64; // Número de chunks a serem agrupados antes de salvar
    public static final boolean ENABLE_ASYNC_COMPRESSION = true; // Habilita compressão assíncrona para chunks
    
    // Configurações para SoundOptimizer
    public static final boolean ENABLE_SOUND_CULLING = true;
    public static final int SOUND_CULLING_DISTANCE = 32; // Distância máxima para ouvir sons
    
    // Configurações para HudOptimizer
    public static final boolean ENABLE_HUD_CACHING = true;
    public static final boolean ENABLE_FONT_CACHING = true;
    public static final int HUD_UPDATE_INTERVAL_TICKS = 5; // Frequência de atualização de certos elementos da HUD
}