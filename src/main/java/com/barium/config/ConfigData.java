package com.barium.config;

public class ConfigData {

    // ================== Modo Agressivo ================== //
    public boolean ENABLE_AGGRESSIVE_OPTIMIZATION = false;

    // ================== Chunk Performance ================== //
    public boolean ENABLE_FRUSTUM_CHUNK_CULLING = true;
    public boolean ENABLE_FLOOD_FILL_CULLING = true;
    public boolean ENABLE_OCCLUSION_CULLING = true;
    public boolean ENABLE_VISIBILITY_GRAPH_CULLING = true;
    public boolean ENABLE_ADVANCED_SECTION_CULLING = false;
    public boolean ENABLE_ENCLOSED_SECTION_CULLING = false;
    public boolean ENABLE_EMPTY_CHUNK_SECTION_CULLING = true;
    // ================== Forest / Tree Optimizations ================== //
    // Quando ativado, se uma seção de chunk contiver principalmente folhas,
    // ela será ignorada na construção para reduzir o custo de renderização em florestas densas.
    public boolean ENABLE_FOREST_SECTION_CULLING = true;
    // Percentual (0.0 - 1.0) da amostra que precisa ser folhas para considerar a seção "floresta".
    public double FOREST_SECTION_LEAF_THRESHOLD = 0.85;
    public boolean ENABLE_CHUNK_UPDATE_THROTTLING = true;
    public boolean ENABLE_DIRECTIONAL_CHUNK_LOADING = true;
    public int MAX_CHUNK_UPLOADS_PER_FRAME = 4;

    // Force an effective render distance. If > 0, the engine behaves as if the player's
    // render distance is this value (useful to simulate lower render distances for performance).
    // 0 = disabled
    public int EFFECTIVE_RENDER_DISTANCE = 0;

    // Sparse chunk rendering factor. 1 = normal; 2 = render every 2nd chunk; etc.
    // Use this to make a higher render distance behave like fewer chunks visually.
    public int SPARSE_CHUNK_FACTOR = 1;

    // Number of chunks around the player which are kept at full update rate.
    // Chunks outside this radius are updated less frequently according to CHUNK_UPDATE_SKIP_RATE.
    public int DETAILED_RENDER_RADIUS = 2;
    // Update only once every N ticks for chunks outside the detailed radius. 1 = every tick (no skipping)
    public int CHUNK_UPDATE_SKIP_RATE = 3;

    // ================== Culling & Level of Detail (LOD) ================== //
    public boolean ENABLE_ENTITY_CULLING = true;
    public double MAX_ENTITY_RENDER_DISTANCE_SQ = 72 * 72;
    public boolean ENABLE_ENTITY_FRUSTUM_CULLING = true;
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
    public boolean ENABLE_PARTICLE_FRUSTUM_CULLING = true;
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
    public boolean DISABLE_TRANSLUCENT_RENDERING = false;
    public boolean DISABLE_TEXTURE_ANIMATIONS = false;
    public boolean ENABLE_GUI_OPTIMIZATION = true;
    public int MIPMAP_LEVEL_OVERRIDE = 0;

    // ================== Game Logic & Tick Optimizations ================== //
    public boolean ENABLE_ENTITY_TICK_CULLING = true;
    public double ENTITY_TICK_CULLING_DISTANCE_SQ = 64 * 64;
    public boolean REDUCE_AMBIENT_PARTICLES = true;
    public boolean ENABLE_HOPPER_TICK_CULLING = true;
}