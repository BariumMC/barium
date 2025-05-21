package com.barium.config;

/**
 * Configuração central para o mod Barium.
 * Contém todas as configurações para os diferentes sistemas de otimização.
 */
public class BariumConfig {
    // --- Configurações Gerais do Mod ---
    public static final boolean ENABLE_MOD_OPTIMIZATIONS = true; // Chave mestre para ativar/desativar todas as otimizações do Barium
    public static final boolean ENABLE_DEBUG_LOGGING = false; // Habilita logs detalhados para depuração

    // --- Configurações para PathfindingOptimizer ---
    public static final int PATH_CACHE_SIZE = 128; // Tamanho máximo do cache de caminhos
    public static final int PATH_UPDATE_INTERVAL_TICKS = 10; // Frequência de atualização dos caminhos para entidades distantes
    public static final int PATH_UPDATE_DISTANCE = 32; // Distância a partir da qual o pathfinding é otimizado

    // --- Configurações para BlockTickOptimizer ---
    public static final boolean ENABLE_ON_DEMAND_TICKING = true; // Habilita o ticking de blocos apenas quando necessário
    public static final int HOPPER_OPTIMIZATION_LEVEL = 2; // 0=desabilitado, 1=básico, 2=avançado (reduz ticks de funil)
    public static final int BLOCK_TICK_REDUCTION_DISTANCE = 64; // Distância para começar a reduzir ticks de blocos
    public static final float BLOCK_TICK_REDUCTION_FACTOR = 0.5f; // Fator de redução de ticks (ex: 0.5f = 50% menos ticks)

    // --- Configurações para RedstoneOptimizer ---
    public static final boolean ENABLE_SIGNAL_COMPRESSION = true; // Otimiza a propagação de sinais de redstone
    public static final int MAX_REDSTONE_UPDATES_PER_TICK = 1024; // Limite de atualizações de redstone por tick
    public static final boolean ENABLE_REDSTONE_DEFERRAL = true; // Adia atualizações de redstone não críticas

    // --- Configurações para ParticleOptimizer ---
    public static final boolean ENABLE_PARTICLE_CULLING = true;
    public static final double PARTICLE_CULLING_DISTANCE = 128.0; // Distância máxima para renderizar partículas
    public static final double PARTICLE_CULLING_FOV_THRESHOLD = 0.5; // Limite de produto escalar para FOV (0.5 ~ 60 graus de FOV)

    public static final boolean ENABLE_PARTICLE_LOD = true;
    public static final double PARTICLE_LOD_DISTANCE_START = 32.0; // Distância para começar a aplicar LOD
    public static final double PARTICLE_LOD_STEP_DISTANCE = 16.0; // A cada X blocos após PARTICLE_LOD_DISTANCE_START, aumenta o nível de LOD
    public static final int MAX_PARTICLE_LOD_LEVELS = 3; // Níveis de LOD máximos (0=máximo, 1, 2, 3)
    public static final int MAX_TOTAL_PARTICLES = 10000; // Limite máximo de partículas renderizadas ao mesmo tempo
    public static final boolean REDUCE_PARTICLE_EMISSION = false; // Reduz a emissão de novas partículas globalmente (ex: em explosões)

    // --- Configurações para EntityTickOptimizer ---
    public static final int ENTITY_FREEZE_DISTANCE = 48; // Distância para entidades começarem a ter ticks reduzidos ou congelados
    public static final int ENTITY_TICK_REDUCTION_DISTANCE = 32; // Distância para começar a reduzir a frequência de ticks
    public static final float ENTITY_TICK_REDUCTION_FACTOR = 0.75f; // Fator de redução (0.75 = 75% menos ticks a distância)
    public static final boolean OPTIMIZE_ITEM_ENTITIES = true; // Otimiza entidades de item (ex: merge mais agressivo)
    public static final boolean CULL_INVISIBLE_ENTITIES = true; // Não ticka ou renderiza entidades invisíveis ao jogador

    // --- Configurações para InventoryOptimizer ---
    public static final boolean ENABLE_SLOT_CACHING = true; // Cache de informações de slots de inventário para renderização
    public static final boolean OPTIMIZE_ITEM_TOOLTIPS = true; // Otimiza o render das tooltips de itens

    // --- Configurações para ChunkSavingOptimizer ---
    public static final int CHUNK_SAVE_BUFFER_SIZE = 64; // Número de chunks a serem agrupados antes de salvar
    public static final boolean ENABLE_ASYNC_COMPRESSION = true; // Habilita compressão assíncrona para chunks
    public static final int CHUNK_SAVE_INTERVAL_TICKS = 6000; // Intervalo de tempo (ticks) para salvar chunks automaticamente (1 tick = 0.05 seg)

    // --- Configurações para SoundOptimizer ---
    public static final boolean ENABLE_SOUND_CULLING = true;
    public static final int SOUND_CULLING_DISTANCE = 32; // Distância máxima para ouvir sons
    public static final int MAX_CONCURRENT_SOUNDS = 100; // Limite máximo de sons tocando simultaneamente
    public static final boolean REDUCE_DISTANT_SOUND_VOLUME = true; // Reduz o volume de sons distantes em vez de cullar

    // --- Configurações para HudOptimizer ---
    public static final boolean ENABLE_HUD_CACHING = true; // Habilita o cache de elementos da HUD
    public static final boolean ENABLE_FONT_CACHING = true; // Habilita o cache de fontes/textos pré-renderizados
    public static final int HUD_UPDATE_INTERVAL_TICKS = 5; // Frequência de atualização de certos elementos da HUD (ex: F3)
    public static final boolean OPTIMIZE_HOTBAR_RENDERING = true; // Otimiza o desenho da hotbar
    public static final boolean DEFER_RENDER_OVERLAYS = true; // Adia renderização de overlays menos críticos
}