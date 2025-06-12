// --- Substitua o conteúdo em: src/main/java/com/barium/config/ConfigData.java ---
package com.barium.config;

/**
 * Esta classe representa a estrutura dos dados que serão salvos no arquivo JSON.
 * GSON usará os nomes das variáveis aqui como as chaves no JSON.
 * Os valores definidos aqui servirão como os padrões na primeira vez que o arquivo for criado.
 */
public class ConfigData {

    // ================== Chunk Performance Optimizations ================== //
    public boolean ENABLE_FRUSTUM_CHUNK_CULLING = true;
    public boolean ENABLE_EMPTY_CHUNK_SECTION_CULLING = true;
    public boolean ENABLE_CHUNK_UPDATE_THROTTLING = true;
    public int MAX_CHUNK_UPLOADS_PER_FRAME = 2;

    // ================== Culling & Level of Detail (LOD) ================== //
    public boolean ENABLE_ENTITY_OPTIMIZATION = true;
    public boolean ENABLE_ENTITY_CULLING = true;
    public double MAX_ENTITY_RENDER_DISTANCE_SQ = 64 * 64;
    public boolean ENABLE_BLOCK_ENTITY_CULLING = true;
    public double MAX_BLOCK_ENTITY_RENDER_DISTANCE_SQ = 64 * 64;
    public boolean ENABLE_BLOCK_ENTITY_OCCLUSION_CULLING = true;
    public boolean ENABLE_DENSE_FOLIAGE_CULLING = true;
    public int DENSE_FOLIAGE_CULLING_LEVEL = 2;
    public boolean ENABLE_BEACON_BEAM_CULLING = true;
    public double BEACON_BEAM_CULL_DISTANCE_SQ = 128 * 128;
    public boolean ENABLE_ARMOR_STAND_LOD = true;
    public double ARMOR_STAND_LOD_DISTANCE_SQ = 32 * 32;

    // ================== Particle Optimizer ================== //
    public boolean ENABLE_PARTICLE_OPTIMIZATION = true;
    public boolean ENABLE_GLOBAL_PARTICLE_LIMIT = true;
    public int MAX_GLOBAL_PARTICLES = 2000;
    public double MAX_TICK_DISTANCE_SQ = 128 * 128;
    public boolean ENABLE_EXPLOSION_PARTICLE_REDUCTION = true;

    // ================== HUD Optimizer ================== //
    public boolean ENABLE_HUD_OPTIMIZATION = true;
    public boolean CACHE_DEBUG_HUD = true;
    public boolean DISABLE_TOASTS = true;

    // ================== Post-Processing Effects ================== //
    public boolean DISABLE_ENTITY_OUTLINES = false;
    public boolean ENABLE_HALF_RESOLUTION_ENTITY_OUTLINES = true;
    public boolean DISABLE_VIGNETTE = true;

    // --- NOVO: Adicione esta seção ---
    // ================== Game Logic / Tick Optimizations ================== //
    
    /**
     * Se ativado, reduz a frequência de atualização da lógica (IA, movimento)
     * de entidades que estão longe do jogador.
     * Alvo: Otimizar ClientWorld.tickEntities
     */
    public boolean ENABLE_ENTITY_TICK_CULLING = true;
    public double ENTITY_TICK_CULLING_DISTANCE_SQ = 64 * 64;

    /**
     * Se ativado, reduz a frequência de verificação de blocos para criar
     * partículas de ambiente (fumaça de tochas, água pingando, etc).
     * Alvo: Otimizar ClientWorld.animateTicks
     */
    public boolean REDUCE_AMBIENT_PARTICLES = true;
    // ------------------------------------

        // ================== Specific Logic Optimizations ================== //

    /**
     * Se ativado, reduz drasticamente a frequência de atualização de funis que estão
     * longe do jogador, diminuindo o lag em farms e sistemas de armazenamento.
     */
    public boolean ENABLE_HOPPER_TICK_CULLING = true;
    public double HOPPER_TICK_CULLING_DISTANCE_SQ = 48 * 48;

    /**
     * Se ativado, reduz a quantidade de partículas de fumaça e destroços geradas
     * por explosões, suavizando os picos de lag.
     */
    public boolean ENABLE_EXPLOSION_PARTICLE_REDUCTION = true;

    /**
     * Se ativado, feixes de sinalizadores (beacons) muito distantes não serão
     * renderizados, economizando um pouco de performance em bases grandes.
     */
    public boolean ENABLE_BEACON_BEAM_CULLING = true;
    public double BEACON_BEAM_CULL_DISTANCE_SQ = 128 * 128;
}