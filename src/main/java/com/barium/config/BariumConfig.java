package com.barium.config;

/**
 * Configuração central para o mod Barium.
 * Contém todas as configurações para os diferentes sistemas de otimização.
 * Todas as flags revisadas para consistência e correção de uso.
 */
public class BariumConfig {

    // Instância estática para carregar e acessar as configurações
    // public static BariumConfig INSTANCE;

    // ================== Geral ================== //
    public boolean enableDebugLogging = false; // Exemplo de flag de debug

    // ================== Pathfinding Optimizer ================== //
    public boolean enablePathfindingOptimization = true;
    public boolean useSmartCache = true;
    public boolean simplifyCollision = true;
    public boolean reduceOffscreenPathfinding = true;
    public int pathCacheSize = 128;
    public int pathUpdateIntervalTicks = 10;
    public int pathUpdateDistance = 32;

    // ================== Particle Optimizer (Client-side) ================== //
    public boolean enableParticleOptimization = true;
    public boolean enableParticleLOD = true;
    public int particleLODDistance = 32;
    public double maxRenderDistanceSq = 128 * 128; // Adicionado aqui para ser configurável
    public double maxTickDistanceSq = 128 * 128; // Adicionado aqui para ser configurável


    // ================== Hud Optimizer (Client-side) ================== //
    public boolean enableHudOptimization = true;
    public boolean cacheDebugHud = true;
    public boolean reduceHudUpdates = true;
    public boolean enableFontCaching = true;
    public int hudUpdateIntervalTicks = 5;
    public boolean skipHudRender = true;
    public boolean adaptiveHudOptimization = true;
}