package com.barium.config;

public class BariumConfig {

    // ================== Particle Optimizer ================== //
    public static final boolean ENABLE_PARTICLE_OPTIMIZATION = true;
    public static final boolean ENABLE_GLOBAL_PARTICLE_LIMIT = true;
    public static final int MAX_GLOBAL_PARTICLES = 2000;
    public static final double MAX_TICK_DISTANCE_SQ = 128 * 128;

    // ================== Hud Optimizer ================== //
    public static final boolean ENABLE_HUD_OPTIMIZATION = true;
    public static final boolean CACHE_DEBUG_HUD = true;
    public static final boolean DISABLE_TOASTS = true;

    // ================== Chunk & Block Entity Optimizer ================== //
    public static final boolean ENABLE_CHUNK_OPTIMIZATION = true;
    public static final boolean ENABLE_BLOCK_ENTITY_CULLING = true;
    public static final double MAX_BLOCK_ENTITY_RENDER_DISTANCE_SQ = 64 * 64;
    public static final boolean ENABLE_FRUSTUM_CHUNK_CULLING = true;
    public static final boolean ENABLE_BLOCK_ENTITY_OCCLUSION_CULLING = true;

    // ================== Entity & World Rendering Optimizer ================== //
    public static final boolean ENABLE_ENTITY_OPTIMIZATION = true;
    public static final boolean ENABLE_ENTITY_CULLING = true;
    public static final double MAX_ENTITY_RENDER_DISTANCE_SQ = 64 * 64;
    public static final boolean ENABLE_BEACON_BEAM_CULLING = true;
    public static final double BEACON_BEAM_CULL_DISTANCE_SQ = 128 * 128;
    public static final boolean ENABLE_EXPLOSION_PARTICLE_REDUCTION = true;
    public static final boolean ENABLE_ARMOR_STAND_LOD = true;
    public static final double ARMOR_STAND_LOD_DISTANCE_SQ = 32 * 32;
    public static final boolean ENABLE_DENSE_FOLIAGE_CULLING = true;
    public static final int DENSE_FOLIAGE_CULLING_LEVEL = 2;
    
    // ================== Post-Processing Optimizations ================== //
    public static final boolean DISABLE_ENTITY_OUTLINES = false;
    public static final boolean ENABLE_HALF_RESOLUTION_ENTITY_OUTLINES = true;
    public static final boolean DISABLE_VIGNETTE = true;
}