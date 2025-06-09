package com.barium.config;

/**
 * Configuração central para o mod Barium.
 * Contém todas as configurações para os diferentes sistemas de otimização.
 * Todas as flags revisadas para consistência e correção de uso.
 */
public class BariumConfig {

    // ================== Geral ================== //
    // (Adicione flags gerais aqui, como DEBUG ou LOGGING)

    // ================== Pathfinding Optimizer ================== //
    /** Ativa a otimização de pathfinding. */
    public static final boolean ENABLE_PATHFINDING_OPTIMIZATION = true;
    
    /** Ativa cache inteligente para pathfinding. */
    public static final boolean USE_SMART_CACHE = true;

    /** Simplifica verificações de colisão durante o pathfinding. */
    public static final boolean SIMPLIFY_COLLISION = true;

    /** Reduz cálculos de pathfinding para entidades fora da tela. */
    public static final boolean REDUCE_OFFSCREEN_PATHFINDING = true;

    /** Tamanho máximo do cache de paths. */
    public static final int PATH_CACHE_SIZE = 128;

    /** Intervalo de atualização de pathfinding em ticks. */
    public static final int PATH_UPDATE_INTERVAL_TICKS = 10;

    /** Distância máxima para considerar atualização de pathfinding. */
    public static final int PATH_UPDATE_DISTANCE = 32;


    // ================== Particle Optimizer (Client-side) ================== //
    /** Ativa a otimização de partículas, incluindo culling e LOD. */
    public static final boolean ENABLE_PARTICLE_OPTIMIZATION = true;

    /** Ativa Nível de Detalhe (LOD) para partículas. */
    public static final boolean ENABLE_PARTICLE_LOD = true;

    /** Distância para aplicar LOD em partículas. */
    public static final int PARTICLE_LOD_DISTANCE = 32;

    /** Distância quadrada máxima para renderizar partículas. */
    public static final double MAX_RENDER_DISTANCE_SQ = 128 * 128;

    /** Distância quadrada máxima para tick de partículas. */
    public static final double MAX_TICK_DISTANCE_SQ = 128 * 128;


    // ================== Hud Optimizer (Client-side) ================== //
    /** Ativa a otimização geral da HUD. */
    public static final boolean ENABLE_HUD_OPTIMIZATION = true;

    /** Ativa o cache específico para Debug HUD (F3). */
    public static final boolean CACHE_DEBUG_HUD = true;

    /** Reduz atualizações redundantes de elementos da HUD. */
    public static final boolean REDUCE_HUD_UPDATES = true;

    /** Ativa cache de fontes usadas na HUD. */
    public static final boolean ENABLE_FONT_CACHING = true;

    /** Intervalo de atualização da HUD em ticks. */
    public static final int HUD_UPDATE_INTERVAL_TICKS = 5;

    /** Ativa skip da renderização de HUD baseado em delta de tempo. */
    public static final boolean SKIP_HUD_RENDER = true;

    /** Ativa adaptação dinâmica de intervalos de cache baseado no FPS. */
    public static final boolean ADAPTIVE_HUD_OPTIMIZATION = true;

    /** Ativa skip da renderização do Debug HUD (F3) baseado em delta de tempo. */
    public static final boolean SKIP_DEBUG_HUD_RENDER = true;

    // ================== Chunk & Block Entity Optimizer (Client-side) ================== //
    /** Ativa otimizações gerais relacionadas a chunks e renderização de blocos. */
    public static final boolean ENABLE_CHUNK_OPTIMIZATION = true;

    /** Ativa culling de entidades de bloco (baús, fornalhas, etc.) */
    public static final boolean ENABLE_BLOCK_ENTITY_CULLING = true;

    /** Distância quadrada máxima para renderizar entidades de bloco. */
    public static final double MAX_BLOCK_ENTITY_RENDER_DISTANCE_SQ = 64 * 64;

    /** Ativa o culling de chunks baseado na frustum de visão (culling de cone). */
    public static final boolean ENABLE_FRUSTUM_CHUNK_CULLING = true;

    /** Ativa otimização adaptativa de chunks baseada na velocidade do jogador. */
    public static final boolean ENABLE_ADAPTIVE_CHUNK_OPTIMIZATION = true; // NOVA FLAG

        // ================== Entity & Animation Optimizer (Client-side) ================== //
    /** Ativa otimizações gerais relacionadas a entidades. */
    public static final boolean ENABLE_ENTITY_OPTIMIZATION = true;

    /** Ativa o culling de entidades (mobs, itens) com base na distância. */
    public static final boolean ENABLE_ENTITY_CULLING = true;

    /** Distância quadrada máxima para renderizar entidades. */
    public static final double MAX_ENTITY_RENDER_DISTANCE_SQ = 64 * 64;

    /** Ativa a otimização de animação, pulando cálculos para entidades distantes. */
    public static final boolean ENABLE_ANIMATION_CULLING = true;

    /** Distância quadrada para parar de animar entidades. Deve ser menor que a distância de renderização. */
    public static final double ANIMATION_CULL_DISTANCE_SQ = 48 * 48;


    // ================== World Rendering Optimizer (Client-side) ================== //
    /** Ativa otimizações de renderização do mundo, como o clima. */
    public static final boolean ENABLE_WORLD_RENDERING_OPTIMIZATION = true;

    /** Reduz a densidade da chuva/neve para melhorar o FPS. 0 = normal, 1 = 75%, 2 = 50%, 3 = 25% */
    public static final int WEATHER_DENSITY_LEVEL = 2; // Reduz em 50% por padrão

        // ================== High-Level Rendering Culling ================== //
    /** Ativa o culling de conteúdo de molduras distantes (item/mapa). */
    public static final boolean ENABLE_ITEM_FRAME_CULLING = true;
    /** Distância quadrada para parar de renderizar o conteúdo das molduras. */
    public static final double ITEM_FRAME_CULL_DISTANCE_SQ = 24 * 24; // 24 blocos

    /** Ativa o culling de nomes (name tags) de entidades distantes. */
    public static final boolean ENABLE_NAME_TAG_CULLING = true;
    /** Distância quadrada para parar de renderizar os nomes das entidades. */
    public static final double NAME_TAG_CULL_DISTANCE_SQ = 32 * 32; // 32 blocos

    /** Ativa o culling do brilho de encantamento em itens distantes no chão. */
    public static final boolean ENABLE_DROPPED_ITEM_GLINT_CULLING = true;
    /** Distância quadrada para parar de renderizar o brilho de itens no chão. */
    public static final double DROPPED_ITEM_GLINT_CULL_DISTANCE_SQ = 24 * 24; // 24 blocos
}