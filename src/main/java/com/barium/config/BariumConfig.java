package com.barium.config;

/**
 * Configuração central para o mod Barium.
 * Contém todas as configurações para os diferentes sistemas de otimização.
 * Corrigido: Adicionadas/Renomeadas flags ausentes/incorretas para corrigir erros de compilação.
 */
public class BariumConfig {

    // --- Geral --- //
    // (Adicione flags gerais se necessário)

    // --- PathfindingOptimizer --- //
    public static final boolean ENABLE_PATHFINDING_OPTIMIZATION = true;
    public static final boolean USE_SMART_CACHE = true;
    public static final boolean SIMPLIFY_COLLISION = true;
    public static final boolean REDUCE_OFFSCREEN_PATHFINDING = true;
    public static final int PATH_CACHE_SIZE = 128;
    public static final int PATH_UPDATE_INTERVAL_TICKS = 10;
    public static final int PATH_UPDATE_DISTANCE = 32;
k

    // --- ParticleOptimizer (Client-side) --- //
    public static final boolean ENABLE_PARTICLE_OPTIMIZATION = true; // Renomeado de ENABLE_PARTICLE_CULLING para consistência
    public static final boolean ENABLE_PARTICLE_LOD = true;
    public static final int PARTICLE_LOD_DISTANCE = 32;

    // --- HudOptimizer (Client-side) --- //
    public static final boolean ENABLE_HUD_OPTIMIZATION = true; // Renomeado de ENABLE_HUD_CACHING
    public static final boolean CACHE_DEBUG_HUD = true; // Adicionado (usado em HudOptimizer)
    public static final boolean REDUCE_HUD_UPDATES = true; // Adicionado (usado em HudOptimizer)
    public static final boolean ENABLE_FONT_CACHING = true;
    public static final int HUD_UPDATE_INTERVAL_TICKS = 5;

}
