package com.barium.config;

// Esta classe agora serve apenas como um contêiner para as configurações em tempo de execução.
// Não é mais final, para que possamos alterar os valores.
// Em uma implementação futura, isso seria carregado de um arquivo.
public class BariumConfig {

    // ================== Particle Optimizer ================== //
    public static boolean ENABLE_PARTICLE_OPTIMIZATION = true;
    public static boolean ENABLE_GLOBAL_PARTICLE_LIMIT = true;
    public static int MAX_GLOBAL_PARTICLES = 2000;
    public static double MAX_TICK_DISTANCE_SQ = 128 * 128;

    // ================== Hud Optimizer ================== //
    public static boolean ENABLE_HUD_OPTIMIZATION = true;
    public static boolean CACHE_DEBUG_HUD = true;
    public static boolean DISABLE_TOASTS = true;

    // ================== Chunk & Block Entity Optimizer ================== //
    public static boolean ENABLE_CHUNK_OPTIMIZATION = true;
    public static boolean ENABLE_BLOCK_ENTITY_CULLING = true;
    public static double MAX_BLOCK_ENTITY_RENDER_DISTANCE_SQ = 64 * 64;
    public static boolean ENABLE_FRUSTUM_CHUNK_CULLING = true;
    public static boolean ENABLE_BLOCK_ENTITY_OCCLUSION_CULLING = true;

    // ================== Entity & World Rendering Optimizer ================== //
    public static boolean ENABLE_ENTITY_OPTIMIZATION = true;
    public static boolean ENABLE_ENTITY_CULLING = true;
    public static double MAX_ENTITY_RENDER_DISTANCE_SQ = 64 * 64;
    public static boolean ENABLE_BEACON_BEAM_CULLING = true;
    public static double BEACON_BEAM_CULL_DISTANCE_SQ = 128 * 128;
    public static boolean ENABLE_EXPLOSION_PARTICLE_REDUCTION = true;
    public static boolean ENABLE_ARMOR_STAND_LOD = true;
    public static double ARMOR_STAND_LOD_DISTANCE_SQ = 32 * 32;
    public static boolean ENABLE_DENSE_FOLIAGE_CULLING = true;
    public static int DENSE_FOLIAGE_CULLING_LEVEL = 2;
    
    // ================== Post-Processing Optimizations ================== //
    public static boolean DISABLE_ENTITY_OUTLINES = false;
    public static boolean ENABLE_HALF_RESOLUTION_ENTITY_OUTLINES = true;
    public static boolean DISABLE_VIGNETTE = true;

    // Aqui podemos adicionar novas configurações no futuro, por exemplo:
    // public static boolean ENABLE_ANIMATION_OPTIMIZATION = true;
}