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

}
