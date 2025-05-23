package com.barium.config;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import com.barium.BariumMod; // Importar BariumMod para o LOGGER

/**
 * Configuração central para o mod Barium.
 * Contém todas as configurações para os diferentes sistemas de otimização.
 * Implementa ConfigData para ser reconhecido pelo AutoConfig/Cloth Config.
 */
@Config(name = "barium_optimization")
public class BariumConfig implements ConfigData {

    // --- Instâncias das Classes Aninhadas de Configuração ---
    // Cada uma dessas variáveis é um objeto que conterá as configurações de uma categoria.
    // Elas são inicializadas com 'new' para garantir que os valores padrão sejam carregados.

    @ConfigEntry.Category("general") // Categoria "general" para o GUI do Cloth Config
    @ConfigEntry.Gui.CollapsibleObject // Torna a categoria colapsável no GUI
    public GeneralSettings GENERAL_SETTINGS = new GeneralSettings();

    // Configurações para PathfindingOptimizer
    @ConfigEntry.Category("pathfinding")
    @ConfigEntry.Gui.CollapsibleObject
    public PathfindingSettings PATHFINDING_OPTIMIZATIONS = new PathfindingSettings();
    
    // Configurações para BlockTickOptimizer
    @ConfigEntry.Category("block_ticking")
    @ConfigEntry.Gui.CollapsibleObject
    public BlockTickSettings BLOCK_TICK_OPTIMIZATIONS = new BlockTickSettings();
    
    // Configurações para RedstoneOptimizer
    @ConfigEntry.Category("redstone")
    @ConfigEntry.Gui.CollapsibleObject
    public RedstoneSettings REDSTONE_OPTIMIZATIONS = new RedstoneSettings();
    
    // Configurações para ParticleOptimizer
    @ConfigEntry.Category("particles")
    @ConfigEntry.Gui.CollapsibleObject
    public ParticleSettings PARTICLE_OPTIMIZATIONS = new ParticleSettings();

    // Configurações para EntityTickOptimizer
    @ConfigEntry.Category("entity_ticking")
    @ConfigEntry.Gui.CollapsibleObject
    public EntityTickSettings ENTITY_TICK_OPTIMIZATIONS = new EntityTickSettings();
    
    // Configurações para InventoryOptimizer
    @ConfigEntry.Category("inventory")
    @ConfigEntry.Gui.CollapsibleObject
    public InventorySettings INVENTORY_OPTIMIZATIONS = new InventorySettings();
    
    // Configurações para ChunkSavingOptimizer
    @ConfigEntry.Category("chunk_saving")
    @ConfigEntry.Gui.CollapsibleObject
    public ChunkSavingSettings CHUNK_SAVING_OPTIMIZATIONS = new ChunkSavingSettings();
    
    // Configurações para SoundOptimizer
    @ConfigEntry.Category("sound")
    @ConfigEntry.Gui.CollapsibleObject
    public SoundSettings SOUND_OPTIMIZATIONS = new SoundSettings();
    
    // Configurações para HudOptimizer
    @ConfigEntry.Category("hud_performance")
    @ConfigEntry.Gui.CollapsibleObject
    public HudPerformanceSettings HUD_OPTIMIZATIONS = new HudPerformanceSettings();

    // Configurações para Text Rendering (para profiling/futuras extensões)
    @ConfigEntry.Category("text_rendering")
    @ConfigEntry.Gui.CollapsibleObject
    public TextRenderingSettings TEXT_RENDERING_OPTIMIZATIONS = new TextRenderingSettings();

    // Configurações para Tooltip & Inventário
    @ConfigEntry.Category("tooltip_inventory")
    @ConfigEntry.Gui.CollapsibleObject
    public TooltipInventorySettings TOOLTIP_INVENTORY_OPTIMIZATIONS = new TooltipInventorySettings();

    // Configurações para ClientTerrainOptimizer
    @ConfigEntry.Category("client_terrain")
    @ConfigEntry.Gui.CollapsibleObject
    public ClientTerrainSettings CLIENT_TERRAIN_OPTIMIZATIONS = new ClientTerrainSettings();


    // --- Classes Aninhadas para Agrupar as Configurações ---
    // Cada classe interna agrupa as configurações de uma área específica.
    // As variáveis dentro dessas classes NÃO são 'static' nem 'final'.

    public static class GeneralSettings {
        public boolean ENABLE_MOD_OPTIMIZATIONS = true; // Chave mestre para todas as otimizações
        public boolean ENABLE_DEBUG_LOGGING = false; // Habilita/desabilita logs de depuração detalhados
    }

    public static class PathfindingSettings {
        public int PATH_CACHE_SIZE = 128; // Tamanho do cache de rotas
        public int PATH_UPDATE_INTERVAL_TICKS = 10; // Frequência de atualização de rota para mobs distantes
        public int PATH_UPDATE_DISTANCE = 32; // Distância para considerar um mob "distante"
    }

    public static class BlockTickSettings {
        public boolean ENABLE_ON_DEMAND_TICKING = true; // Habilita ticking de blocos sob demanda
        @ConfigEntry.BoundedDiscrete(min = 0, max = 2) // Limita a opção a 0, 1 ou 2 no GUI
        public int HOPPER_OPTIMIZATION_LEVEL = 2; // Nível de otimização de hoppers: 0=off, 1=reduz frequência, 2=baseado em mudança
    }
    
    public static class RedstoneSettings {
        public boolean ENABLE_SIGNAL_COMPRESSION = true; // Habilita compactação de sinais de redstone
        public int MAX_REDSTONE_UPDATES_PER_TICK = 1024; // Limite de atualizações de redstone por tick
    }
    
    public static class ParticleSettings {
        public boolean ENABLE_PARTICLE_CULLING = true; // Habilita descarte de partículas fora da visão/distância
        public boolean ENABLE_PARTICLE_LOD = true; // Habilita Nível de Detalhe para partículas
        public double PARTICLE_LOD_DISTANCE_START = 32.0; // Distância para iniciar o LOD de partículas
        public double PARTICLE_LOD_STEP_DISTANCE = 16.0; // Intervalo de distância para o próximo nível de LOD
        @ConfigEntry.BoundedDiscrete(min = 1, max = 5)
        public int MAX_PARTICLE_LOD_LEVELS = 3; // Número máximo de níveis de LOD para partículas
        public double PARTICLE_CULLING_DISTANCE_SQ = 64.0 * 64.0; // Distância quadrada para descarte de partículas (64 blocos)
        public int MAX_TOTAL_PARTICLES = 10000; // Limite máximo de partículas totais renderizadas/tickadas
    }

    public static class EntityTickSettings {
        public int ENTITY_FREEZE_DISTANCE = 48; // Distância para "congelar" entidades (reduzir tick rate)
    }
    
    public static class InventorySettings {
        public boolean ENABLE_SLOT_CACHING = true; // Habilita cache de slots de inventário
    }
    
    public static class ChunkSavingSettings {
        public int CHUNK_SAVE_BUFFER_SIZE = 64; // Quantos chunks salvar em um único lote
        public boolean ENABLE_ASYNC_COMPRESSION = true; // Habilita compressão assíncrona para salvamento de chunks
    }
    
    public static class SoundSettings {
        public boolean ENABLE_SOUND_CULLING = true; // Habilita descarte de sons não audíveis
        public int SOUND_CULLING_DISTANCE = 32; // Distância para descarte de sons
    }
    
    public static class HudPerformanceSettings {
        public boolean ENABLE_HUD_CACHING = true; // Habilita cache geral de elementos da HUD (incluindo F3)
        public boolean ENABLE_DIRTY_FLAG_OPTIMIZATION = true; // Otimiza barras de vida/fome/armadura e hotbar por dirty flag
        public boolean ENABLE_CENTRALIZED_TEXT_BATCHING = true; // Garante que o batching de texto é flusheado de forma centralizada
        @ConfigEntry.BoundedDiscrete(min = 1, max = 60)
        public int HUD_UPDATE_INTERVAL_TICKS = 5; // Frequência de atualização de elementos gerais da HUD (e.g., info F3)
    }

    public static class TextRenderingSettings {
        public boolean ENABLE_TEXT_PROFILING = false; // Apenas para depuração: adiciona seções no profiler para renderização de texto
    }

    public static class TooltipInventorySettings {
        public boolean ENABLE_TOOLTIP_CACHING = true; // Habilita cache de tooltips (o que é implementado)
        public boolean DISABLE_TOOLTIP_GRADIENTS = false; // Substitui gradientes de fundo do tooltip por cor sólida
    }

    public static class ClientTerrainSettings {
        public boolean ENABLE_TERRAIN_STREAMING = true; // Habilita streaming de terreno (culling)
        public boolean ENABLE_DIRECTIONAL_PRELOADING = true; // Habilita pré-carregamento de chunks na direção do movimento
        public boolean ENABLE_CHUNK_LOD = true; // Habilita Nível de Detalhe para chunks
        public int CHUNK_LOD_DISTANCE_LEVEL1 = 48; // Distância para LOD level 1
        public int CHUNK_LOD_DISTANCE_LEVEL2 = 96; // Distância para LOD level 2
        @ConfigEntry.BoundedDiscrete(min = 1, max = 60)
        public int CHUNK_UPDATE_INTERVAL_LOD1 = 5; // Re-meshing a cada 5 ticks para LOD 1
        @ConfigEntry.BoundedDiscrete(min = 1, max = 60)
        public int CHUNK_UPDATE_INTERVAL_LOD2 = 10; // Re-meshing a cada 10 ticks para LOD 2
        public double MOVEMENT_ALIGNMENT_THRESHOLD = 0.6; // Limiar de alinhamento para considerar "na frente" do jogador
    }


    // --- Métodos de Singleton e Registro do AutoConfig ---
    // Esses métodos são essenciais para o funcionamento do Cloth Config.

    private static BariumConfig instance;

    /**
     * Retorna a instância singleton da configuração.
     * Garante que AutoConfig já foi registrado.
     * @return A instância de BariumConfig.
     */
    public static BariumConfig getInstance() {
        if (instance == null) {
            // Tenta obter a instância do AutoConfig. Se falhar (ex: chamado antes do Fabric Mod inicializar
            // ou sem ModMenu/ClothConfig), um fallback para uma instância padrão é usado.
            try {
                instance = AutoConfig.getConfigHolder(BariumConfig.class).getConfig();
            } catch (Exception e) {
                // Loga o erro, mas permite que o mod continue funcionando com configurações padrão.
                // Isso é importante em ambientes de desenvolvimento ou se o Cloth Config não for carregado.
                BariumMod.LOGGER.error("BariumConfig not yet registered with AutoConfig, falling back to default instance. Error: " + e.getMessage());
            }
        }
        return instance;
    }

    /**
     * Registra as configurações do AutoConfig.
     * Deve ser chamado no ModInitializer (em BariumMod.java).
     */
    public static void registerConfigs() {
        AutoConfig.register(BariumConfig.class, JanksonConfigSerializer::new);
    }
}