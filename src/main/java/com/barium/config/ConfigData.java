package com.barium.config;

/**
 * Esta classe representa a estrutura dos dados que serão salvos no arquivo JSON.
 */
public class ConfigData {

    // ================== Chunk Performance Optimizations ================== //
    public boolean ENABLE_FRUSTUM_CHUNK_CULLING = true;
    public boolean ENABLE_EMPTY_CHUNK_SECTION_CULLING = true;
    public boolean ENABLE_CHUNK_UPDATE_THROTTLING = true;
    public int MAX_CHUNK_UPLOADS_PER_FRAME = 2;

    // ================== Culling & Level of Detail (LOD) ================== //
    public boolean ENABLE_ENTITY_OPTIMIZATION = true;
    public boolean ENABLE_ENTITY_CULLING = true;
    public double MAX_ENTITY_RENDER_DISTANCE_SQ = 64 * 64;
    public boolean ENABLE_BLOCK_ENTITY_CULLING = true;
    public double MAX_BLOCK_ENTITY_RENDER_DISTANCE_SQ = 64 * 64;
    public boolean ENABLE_BLOCK_ENTITY_OCCLUSION_CULLING = true;
    public boolean ENABLE_DENSE_FOLIAGE_CULLING = true;
    public int DENSE_FOLIAGE_CULLING_LEVEL = 2;
    public boolean ENABLE_BEACON_BEAM_CULLING = true; // Já existia
    public double BEACON_BEAM_CULL_DISTANCE_SQ = 128 * 128; // Já existia
    public double BLOCK_ENTITY_OCCLUSION_MIN_DISTANCE_SQ = 8 * 8;
    public boolean ENABLE_ARMOR_STAND_LOD = true;
    public double ARMOR_STAND_LOD_DISTANCE_SQ = 32 * 32;

    // ================== Particle Optimizer ================== //
    public boolean ENABLE_PARTICLE_OPTIMIZATION = true;
    public boolean ENABLE_GLOBAL_PARTICLE_LIMIT = true;
    public int MAX_GLOBAL_PARTICLES = 2000;
    public double MAX_TICK_DISTANCE_SQ = 128 * 128;
    public boolean ENABLE_EXPLOSION_PARTICLE_REDUCTION = true; // Já existia

    // ================== HUD Optimizer ================== //
    public boolean ENABLE_HUD_OPTIMIZATION = true;
    public boolean CACHE_DEBUG_HUD = true;
    public boolean DISABLE_TOASTS = true;

    // ================== Post-Processing Effects ================== //
    public boolean DISABLE_ENTITY_OUTLINES = false;
    public boolean ENABLE_HALF_RESOLUTION_ENTITY_OUTLINES = true;
    public boolean DISABLE_VIGNETTE = true;

    // ================== Game Logic / Tick Optimizations ================== //
    public boolean ENABLE_ENTITY_TICK_CULLING = true;
    public double ENTITY_TICK_CULLING_DISTANCE_SQ = 64 * 64;
    public boolean REDUCE_AMBIENT_PARTICLES = true;

    // ================== Specific Logic Optimizations ================== //
    // Apenas a otimização de Hopper é realmente nova aqui.
    public boolean ENABLE_HOPPER_TICK_CULLING = true;
    public double HOPPER_TICK_CULLING_DISTANCE_SQ = 48 * 48;
}