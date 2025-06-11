// --- Substitua o conteúdo em: src/main/java/com/barium/config/BariumConfig.java ---
package com.barium.config;

/**
 * Contém todas as configurações do mod Barium.
 * Estes valores são alterados em tempo de execução pela tela de configuração (BariumModMenu)
 * e são usados pelos Mixins e classes de otimização para decidir como se comportar.
 */
public class BariumConfig {

    // ================== Chunk Performance Optimizations ================== //
    /**
     * Se ativado, o Barium tentará pular a construção de chunks que estão fora
     * do campo de visão da câmera (frustum culling).
     */
    public static boolean ENABLE_FRUSTUM_CHUNK_CULLING = true;
    
    /**
     * Se ativado, seções de chunk (16x16x16) que contêm apenas ar serão puladas durante a
     * fase de reconstrução, economizando muita CPU.
     */
    public static boolean ENABLE_EMPTY_CHUNK_SECTION_CULLING = true;

    /**
     * Se ativado, limita o número de chunks enviados para a GPU a cada frame.
     * Isso transforma grandes picos de lag em atualizações menores e mais suaves,
     * reduzindo drasticamente as travadas (stutters) ao se mover.
     */
    public static boolean ENABLE_CHUNK_UPDATE_THROTTLING = true;

    /**
     * O número máximo de chunks a serem enviados para a GPU por frame.
     * Usado somente se ENABLE_CHUNK_UPDATE_THROTTLING for verdadeiro.
     */
    public static int MAX_CHUNK_UPLOADS_PER_FRAME = 2;


    // ================== Culling & Level of Detail (LOD) ================== //
    public static boolean ENABLE_ENTITY_OPTIMIZATION = true;
    public static boolean ENABLE_ENTITY_CULLING = true;
    public static double MAX_ENTITY_RENDER_DISTANCE_SQ = 64 * 64;

    public static boolean ENABLE_BLOCK_ENTITY_CULLING = true;
    public static double MAX_BLOCK_ENTITY_RENDER_DISTANCE_SQ = 64 * 64;
    public static boolean ENABLE_BLOCK_ENTITY_OCCLUSION_CULLING = true;

    public static boolean ENABLE_DENSE_FOLIAGE_CULLING = true;
    public static int DENSE_FOLIAGE_CULLING_LEVEL = 2;

    public static boolean ENABLE_BEACON_BEAM_CULLING = true;
    public static double BEACON_BEAM_CULL_DISTANCE_SQ = 128 * 128;

    public static boolean ENABLE_ARMOR_STAND_LOD = true;
    public static double ARMOR_STAND_LOD_DISTANCE_SQ = 32 * 32;


    // ================== Particle Optimizer ================== //
    public static boolean ENABLE_PARTICLE_OPTIMIZATION = true;
    public static boolean ENABLE_GLOBAL_PARTICLE_LIMIT = true;
    public static int MAX_GLOBAL_PARTICLES = 2000;
    public static double MAX_TICK_DISTANCE_SQ = 128 * 128;
    public static boolean ENABLE_EXPLOSION_PARTICLE_REDUCTION = true;


    // ================== HUD Optimizer ================== //
    public static boolean ENABLE_HUD_OPTIMIZATION = true;
    public static boolean CACHE_DEBUG_HUD = true;
    public static boolean DISABLE_TOASTS = true;


    // ================== Post-Processing Effects ================== //
    public static boolean DISABLE_ENTITY_OUTLINES = false;
    public static boolean ENABLE_HALF_RESOLUTION_ENTITY_OUTLINES = true;
    public static boolean DISABLE_VIGNETTE = true;
}