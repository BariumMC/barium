package com.barium.config;

public class ConfigData {

    // ================== Chunk Performance ================== //
    public boolean ENABLE_FRUSTUM_CHUNK_CULLING = true;

    // --- NOVA OTIMIZAÇÃO: Flood-Fill (Estável) ---
    // Ativada por padrão como a principal otimização de visibilidade.
    public boolean ENABLE_FLOOD_FILL_CULLING = true;

    // --- NOVA OTIMIZAÇÃO: Oclusão Total (Estável) ---
    // Ótima para cavernas e subsolo, sem bugs visuais.
    public boolean ENABLE_OCCLUSION_CULLING = true;

    // --- Otimizações Experimentais Antigas (Agora Desativadas por Padrão) ---
    public boolean ENABLE_VISIBILITY_GRAPH_CULLING = false; // Substituída pelo Flood-Fill
    public boolean ENABLE_ADVANCED_SECTION_CULLING = false;
    public boolean ENABLE_ENCLOSED_SECTION_CULLING = false; // Substituída pela Oclusão Total

    public boolean ENABLE_EMPTY_CHUNK_SECTION_CULLING = true;
    public boolean ENABLE_CHUNK_UPDATE_THROTTLING = true;
    public int MAX_CHUNK_UPLOADS_PER_FRAME = 4;

    // ================== Culling & Level of Detail (LOD) ================== //
    public boolean ENABLE_ENTITY_CULLING = true;
    public double MAX_ENTITY_RENDER_DISTANCE_SQ = 72 * 72;
    public boolean ENABLE_BLOCK_ENTITY_CULLING = true;
    public double MAX_BLOCK_ENTITY_RENDER_DISTANCE_SQ = 72 * 72;
    public boolean ENABLE_BLOCK_ENTITY_OCCLUSION_CULLING = true;
    public double BLOCK_ENTITY_OCCLUSION_MIN_DISTANCE_SQ = 8 * 8;
    public boolean ENABLE_DENSE_FOLIAGE_CULLING = true;
    public int DENSE_FOLIAGE_CULLING_LEVEL = 2;
    public boolean ENABLE_BEACON_BEAM_OPTIMIZATION = true;
    public double BEACON_BEAM_CULL_DISTANCE_SQ = 256 * 256;

    // ================== Particle Optimizer ================== //
    public boolean ENABLE_PARTICLE_OPTIMIZATION = true;
    public double PARTICLE_CULL_DISTANCE_SQ = 128 * 128;
    public boolean ENABLE_EXPLOSION_PARTICLE_REDUCTION = true;
    public boolean ENABLE_GLOBAL_PARTICLE_LIMIT = true;
    public int MAX_GLOBAL_PARTICLES = 4096;

    // ================== Visual Effects & HUD ================== //
    public boolean CACHE_DEBUG_HUD = true;
    public boolean ENABLE_TOOLTIP_CACHING = true;
    public boolean DISABLE_TOASTS = true;
    public boolean DISABLE_VIGNETTE = true;
    public boolean DISABLE_ENTITY_OUTLINES = false;
    public boolean ENABLE_HALF_RESOLUTION_ENTITY_OUTLINES = true;
    public boolean ENABLE_ADAPTIVE_FOG = true;
    public int ADAPTIVE_FOG_TARGET_FPS = 58;

    // ================== Game Logic & Tick Optimizations ================== //
    public boolean ENABLE_ENTITY_TICK_CULLING = true;
    public double ENTITY_TICK_CULLING_DISTANCE_SQ = 64 * 64;
    public boolean REDUCE_AMBIENT_PARTICLES = true;
    public boolean ENABLE_HOPPER_TICK_CULLING = true;
}