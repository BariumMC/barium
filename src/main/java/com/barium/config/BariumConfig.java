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

    // --- Geometric Optimization (Client-side) --- //
    public static final boolean ENABLE_GEOMETRIC_OPTIMIZATION = true; // Flag geral

    // LOD/Simplificação de Malhas
    public static final boolean ENABLE_MESH_LOD = true;
    public static final int LOD_DISTANCE_HIGH = 32; // Distância para LOD de alto nível
    public static final int LOD_DISTANCE_MEDIUM = 64; // Distância para LOD de médio nível
    public static final int LOD_DISTANCE_LOW = 128; // Distância para LOD de baixo nível

    // Instancing / Impostors
    public static final boolean ENABLE_VEGETATION_INSTANCING = true;
    public static final int INSTANCING_DISTANCE_MAX = 48; // Distância máxima para usar instancing

    public static final boolean ENABLE_VEGETATION_IMPOSTORS = true;
    public static final int IMPOSTOR_DISTANCE_MIN = 64; // Distância mínima para usar impostors
    public static final int IMPOSTOR_DISTANCE_MAX = 256; // Distância máxima para renderizar impostors

    // Occlusion Culling (Pre-pass)
    public static final boolean ENABLE_CHUNK_OCCLUSION_CULLING = true;
    public static final int OCCLUSION_UPDATE_INTERVAL_TICKS = 20; // Frequência de atualização do culling
}