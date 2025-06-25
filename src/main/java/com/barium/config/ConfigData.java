package com.barium.config;

/**
 * Esta classe representa a estrutura dos dados que serão salvos no arquivo JSON.
 * Cada campo público aqui corresponde a uma opção de configuração para o Barium.
 * Os valores iniciais servem como os padrões para uma nova configuração.
 */
public class ConfigData {

    // ================== Chunk Performance ================== //
    public boolean ENABLE_FRUSTUM_CHUNK_CULLING = true;
    public boolean ENABLE_EMPTY_CHUNK_SECTION_CULLING = true;
    public boolean ENABLE_CHUNK_UPDATE_THROTTLING = true;
    public int MAX_CHUNK_UPLOADS_PER_FRAME = 2;
    public boolean ENABLE_DISTANCE_THROTTLING = true;
    // Adicionando a nova otimização de chunk
    public boolean ENABLE_VISIBILITY_GRAPH_CULLING = true;

    // ================== Culling & Level of Detail (LOD) ================== //
    // Adicionando as otimizações de entidade que faltavam
    public boolean ENABLE_ENTITY_OPTIMIZATION = true; // Flag geral para otimizações de entidade
    public boolean ENABLE_ENTITY_CULLING = true;
    public double MAX_ENTITY_RENDER_DISTANCE_SQ = 64 * 64;
    
    public boolean ENABLE_BLOCK_ENTITY_CULLING = true;
    public double MAX_BLOCK_ENTITY_RENDER_DISTANCE_SQ = 64 * 64;
    
    public boolean ENABLE_BLOCK_ENTITY_OCCLUSION_CULLING = true;
    public double BLOCK_ENTITY_OCCLUSION_MIN_DISTANCE_SQ = 8 * 8;
    
    public boolean ENABLE_DENSE_FOLIAGE_CULLING = true;
    public int DENSE_FOLIAGE_CULLING_LEVEL = 2;
    
    public boolean ENABLE_BEACON_BEAM_CULLING = true;
    public double BEACON_BEAM_CULL_DISTANCE_SQ = 128 * 128;

    // ================== Particle Optimizer ================== //
    public boolean ENABLE_PARTICLE_OPTIMIZATION = true;
    public double MAX_TICK_DISTANCE_SQ = 128 * 128; // Distância para tick e render de partículas
    public boolean ENABLE_GLOBAL_PARTICLE_LIMIT = true;
    public int MAX_GLOBAL_PARTICLES = 4096;
    public boolean ENABLE_EXPLOSION_PARTICLE_REDUCTION = true;

    // ================== HUD Optimizer ================== //
    public boolean ENABLE_HUD_OPTIMIZATION = true;
    public boolean CACHE_DEBUG_HUD = true;
    public boolean DISABLE_TOASTS = true;
    public boolean ENABLE_TOOLTIP_CACHING = true;

    // ================== Visual Effects & Rendering ================== //
    public boolean DISABLE_ENTITY_OUTLINES = false;
    public boolean ENABLE_HALF_RESOLUTION_ENTITY_OUTLINES = true;
    public boolean DISABLE_VIGNETTE = true;
    // Adicionando a nova otimização de névoa
    public boolean ENABLE_ADAPTIVE_FOG = true;
    public int ADAPTIVE_FOG_TARGET_FPS = 58;

    // ================== Game Logic & Tick Optimizations ================== //
    public boolean ENABLE_ENTITY_TICK_CULLING = true;
    public double ENTITY_TICK_CULLING_DISTANCE_SQ = 64 * 64;
    public boolean REDUCE_AMBIENT_PARTICLES = true;
    public boolean ENABLE_HOPPER_TICK_CULLING = true;
    
}