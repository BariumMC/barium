package com.barium.config;

import com.barium.BariumMod; // Necessário para logging

/**
 * Configuração central para o mod Barium.
 * Contém todas as configurações para os diferentes sistemas de otimização.
 */
public class BariumConfig {
    // --- Configurações Gerais do Mod ---
    public static final boolean ENABLE_MOD_OPTIMIZATIONS = true; // Chave mestre para ativar/desativar todas as otimizações do Barium
    public static final boolean ENABLE_DEBUG_LOGGING = false; // Habilita logs detalhados para depuração

    // --- Configurações para PathfindingOptimizer --- (Não implementado nesta seção)
    public static final int PATH_CACHE_SIZE = 128;
    public static final int PATH_UPDATE_INTERVAL_TICKS = 10;
    public static final int PATH_UPDATE_DISTANCE = 32;

    // --- Configurações para BlockTickOptimizer --- (Não implementado nesta seção)
    public static final boolean ENABLE_ON_DEMAND_TICKING = true;
    public static final int HOPPER_OPTIMIZATION_LEVEL = 2;
    public static final int BLOCK_TICK_REDUCTION_DISTANCE = 64;
    public static final float BLOCK_TICK_REDUCTION_FACTOR = 0.5f;

    // --- Configurações para RedstoneOptimizer --- (Não implementado nesta seção)
    public static final boolean ENABLE_SIGNAL_COMPRESSION = true;
    public static final int MAX_REDSTONE_UPDATES_PER_TICK = 1024;
    public static final boolean ENABLE_REDSTONE_DEFERRAL = true;

    // --- Configurações para ParticleOptimizer ---
    public static final boolean ENABLE_PARTICLE_CULLING = true; // Chave para habilitar culling (distância e frustum)
    public static final double PARTICLE_CULLING_DISTANCE = 128.0; // Distância máxima para renderizar partículas
    // Usamos distância ao quadrado para evitar Math.sqrt() em cada cálculo (performance).
    public static final double PARTICLE_CULLING_DISTANCE_SQ = PARTICLE_CULLING_DISTANCE * PARTICLE_CULLING_DISTANCE;
    public static final double PARTICLE_CULLING_FOV_THRESHOLD = 0.5; // Limite de produto escalar para FOV (0.5 ~ 60 graus de FOV)

    public static final boolean ENABLE_PARTICLE_LOD = true; // Chave para habilitar LOD de tick
    public static final double PARTICLE_LOD_DISTANCE_START = 32.0; // Distância para começar a aplicar LOD
    public static final double PARTICLE_LOD_STEP_DISTANCE = 16.0; // A cada X blocos após PARTICLE_LOD_DISTANCE_START, aumenta o nível de LOD
    public static final int MAX_PARTICLE_LOD_LEVELS = 3; // Níveis de LOD máximos (0=máximo, 1, 2, 3)
    public static final int MAX_TOTAL_PARTICLES = 10000; // Limite máximo de partículas ativas ao mesmo tempo (contadas globalmente)
    public static final boolean REDUCE_PARTICLE_EMISSION = false; // Reduz a emissão de novas partículas globalmente (ex: em explosões)

    // --- Configurações para EntityTickOptimizer --- (Não implementado nesta seção)
    public static final int ENTITY_FREEZE_DISTANCE = 48;
    public static final int ENTITY_TICK_REDUCTION_DISTANCE = 32;
    public static final float ENTITY_TICK_REDUCTION_FACTOR = 0.75f;
    public static final boolean OPTIMIZE_ITEM_ENTITIES = true;
    public static final boolean CULL_INVISIBLE_ENTITIES = true;

    // --- Configurações para InventoryOptimizer --- (Não implementado nesta seção)
    public static final boolean ENABLE_SLOT_CACHING = true;
    public static final boolean OPTIMIZE_ITEM_TOOLTIPS = true;

    // --- Configurações para ChunkSavingOptimizer --- (Não implementado nesta seção)
    public static final int CHUNK_SAVE_BUFFER_SIZE = 64;
    public static final boolean ENABLE_ASYNC_COMPRESSION = true;
    public static final int CHUNK_SAVE_INTERVAL_TICKS = 6000;

    // --- Configurações para SoundOptimizer --- (Não implementado nesta seção)
    public static final boolean ENABLE_SOUND_CULLING = true;
    public static final int SOUND_CULLING_DISTANCE = 32;
    public static final int MAX_CONCURRENT_SOUNDS = 100;
    public static final boolean REDUCE_DISTANT_SOUND_VOLUME = true;

    // --- Configurações para HudOptimizer --- (Não implementado nesta seção)
    public static final boolean ENABLE_HUD_CACHING = true;
    public static final boolean ENABLE_FONT_CACHING = true;
    public static final int HUD_UPDATE_INTERVAL_TICKS = 5;
    public static final boolean OPTIMIZE_HOTBAR_RENDERING = true;
    public static final boolean DEFER_RENDER_OVERLAYS = true;

    // Método para imprimir todas as configurações no log, útil para depuração
    public static void printConfig() {
        BariumMod.LOGGER.info("--- Barium Mod Configuration ---");
        BariumMod.LOGGER.info("General Optimizations Enabled: " + ENABLE_MOD_OPTIMIZATIONS);
        BariumMod.LOGGER.info("Debug Logging Enabled: " + ENABLE_DEBUG_LOGGING);

        BariumMod.LOGGER.info("\n--- Particle Optimizer ---");
        BariumMod.LOGGER.info("  Particle Culling Enabled: " + ENABLE_PARTICLE_CULLING);
        BariumMod.LOGGER.info("  Culling Distance: " + PARTICLE_CULLING_DISTANCE + " blocks");
        BariumMod.LOGGER.info("  Culling FOV Threshold (dot product): " + PARTICLE_CULLING_FOV_THRESHOLD);
        BariumMod.LOGGER.info("  Particle LOD Enabled: " + ENABLE_PARTICLE_LOD);
        BariumMod.LOGGER.info("  LOD Start Distance: " + PARTICLE_LOD_DISTANCE_START + " blocks");
        BariumMod.LOGGER.info("  LOD Step Distance: " + PARTICLE_LOD_STEP_DISTANCE + " blocks");
        BariumMod.LOGGER.info("  Max LOD Levels: " + MAX_PARTICLE_LOD_LEVELS);
        BariumMod.LOGGER.info("  Max Total Particles: " + MAX_TOTAL_PARTICLES);
        BariumMod.LOGGER.info("  Reduce Particle Emission: " + REDUCE_PARTICLE_EMISSION);

        BariumMod.LOGGER.info("\n--- Other Optimizers (Configured but not implemented in this snippet) ---");
        BariumMod.LOGGER.info("  Path Cache Size: " + PATH_CACHE_SIZE);
        BariumMod.LOGGER.info("  Hopper Optimization Level: " + HOPPER_OPTIMIZATION_LEVEL);
        BariumMod.LOGGER.info("  Max Redstone Updates Per Tick: " + MAX_REDSTONE_UPDATES_PER_TICK);
        BariumMod.LOGGER.info("  Entity Freeze Distance: " + ENTITY_FREEZE_DISTANCE);
        BariumMod.LOGGER.info("  Enable Slot Caching: " + ENABLE_SLOT_CACHING);
        BariumMod.LOGGER.info("  Chunk Save Buffer Size: " + CHUNK_SAVE_BUFFER_SIZE);
        BariumMod.LOGGER.info("  Enable Sound Culling: " + ENABLE_SOUND_CULLING);
        BariumMod.LOGGER.info("  Enable HUD Caching: " + ENABLE_HUD_CACHING);
        BariumMod.LOGGER.info("--- End Barium Config ---");
    }
}