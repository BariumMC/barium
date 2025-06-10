package com.barium.config;

/**
 * Configuração central para o mod Barium.
 * Contém todas as configurações para os diferentes sistemas de otimização.
 */
public class BariumConfig {

    // ================== Particle Optimizer (Client-side) ================== //
    /** Ativa a otimização de partículas, incluindo o limite global e o culling de tick. */
    public static final boolean ENABLE_PARTICLE_OPTIMIZATION = true;
    /** Ativa um limite máximo global para o número de partículas vivas. */
    public static final boolean ENABLE_GLOBAL_PARTICLE_LIMIT = true;
    /** O número máximo de partículas permitidas no mundo. Um bom valor é entre 1000 e 4000. */
    public static final int MAX_GLOBAL_PARTICLES = 2000;
    /** Distância quadrada máxima para tick de partículas. */
    public static final double MAX_TICK_DISTANCE_SQ = 128 * 128;

    // ================== Hud Optimizer (Client-side) ================== //
    /** Ativa a otimização geral da HUD. */
    public static final boolean ENABLE_HUD_OPTIMIZATION = true;
    /** Ativa o cache específico para Debug HUD (F3). */
    public static final boolean CACHE_DEBUG_HUD = true;
    /** Desativa completamente as notificações (toasts) de receitas e progressos. */
    public static final boolean DISABLE_TOASTS = true;

    // ================== Chunk & Block Entity Optimizer (Client-side) ================== //
    /** Ativa otimizações gerais relacionadas a chunks e renderização de blocos. */
    public static final boolean ENABLE_CHUNK_OPTIMIZATION = true;
    /** Ativa o culling de entidades de bloco (baús, fornalhas, etc.) */
    public static final boolean ENABLE_BLOCK_ENTITY_CULLING = true;
    /** Distância quadrada máxima para renderizar entidades de bloco. */
    public static final double MAX_BLOCK_ENTITY_RENDER_DISTANCE_SQ = 64 * 64;
    /** Ativa o culling de chunks baseado na frustum de visão (usando o WorldRendererMixin otimizado). */
    public static final boolean ENABLE_FRUSTUM_CHUNK_CULLING = true;
    /** Ativa o culling de seções de chunk que contêm apenas ar, evitando que sejam reconstruídas. */
    public static final boolean ENABLE_EMPTY_CHUNK_CULLING = true; // (Atualmente desativado devido a conflitos) -> Vamos manter a flag para o futuro.

    // ================== Entity & World Rendering Optimizer (Client-side) ================== //
    /** Ativa otimizações gerais relacionadas a entidades. */
    public static final boolean ENABLE_ENTITY_OPTIMIZATION = true;
    /** Ativa o culling de entidades (mobs, itens) com base na distância. */
    public static final boolean ENABLE_ENTITY_CULLING = true;
    /** Distância quadrada máxima para renderizar entidades. */
    public static final double MAX_ENTITY_RENDER_DISTANCE_SQ = 64 * 64;
    /** Ativa o culling de feixes de sinalizadores (beacon beams) distantes. */
    public static final boolean ENABLE_BEACON_BEAM_CULLING = true;
    /** Distância quadrada para parar de renderizar os feixes de sinalizadores. */
    public static final double BEACON_BEAM_CULL_DISTANCE_SQ = 128 * 128;
    /** Ativa a redução de partículas em explosões. */
    public static final boolean ENABLE_EXPLOSION_PARTICLE_REDUCTION = true;
    /** Ativa um LOD (Nível de Detalhe) para suportes de armadura, removendo a placa de base à distância. */
    public static final boolean ENABLE_ARMOR_STAND_LOD = true;
    /** Distância quadrada para aplicar o LOD nos suportes de armadura. */
    public static final double ARMOR_STAND_LOD_DISTANCE_SQ = 32 * 32;
    /** Ativa o culling (desbaste) de vegetação densa como grama, samambaias e arbustos. */
    public static final boolean ENABLE_DENSE_FOLIAGE_CULLING = true;
    /** Nível de desbaste. 0 = normal, 1 = 75% renderizado, 2 = 50% renderizado, 3 = 25% renderizado. */
    public static final int DENSE_FOLIAGE_CULLING_LEVEL = 2;

    // ================== Post-Processing Optimizations (From Spark Report) ================== //
    /** Desativa completamente o efeito de brilho em entidades (outlines), que é muito caro. */
    public static final boolean DISABLE_ENTITY_OUTLINES = false; // Desativado por padrão para dar preferência à otimização de meia resolução.
    /** Ativa a otimização de renderização do efeito de brilho, renderizando-o em meia resolução para um grande ganho de FPS. */
    public static final boolean ENABLE_HALF_RESOLUTION_ENTITY_OUTLINES = true;
    /** Desativa completamente o efeito de vinheta (escurecimento das bordas da tela). */
    public static final boolean DISABLE_VIGNETTE = true;

}