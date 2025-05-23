package com.barium.config;

import com.barium.BariumMod;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;

/**
 * Configuração central para o mod Barium.
 * Contém todas as configurações para os diferentes sistemas de otimização.
 */
@Config(name = "barium_optimization")
public class BariumConfig implements ConfigData {

    // General Mod Settings
    @ConfigEntry.Category("general")
    @ConfigEntry.Gui.CollapsibleObject
    public GeneralSettings GENERAL_SETTINGS = new GeneralSettings();

    public static class GeneralSettings {
        public boolean ENABLE_MOD_OPTIMIZATIONS = true; // Master switch for all Barium optimizations
        public boolean ENABLE_DEBUG_LOGGING = false; // Enable/disable verbose debug logging
    }

    // Configurações para PathfindingOptimizer
    @ConfigEntry.Category("pathfinding")
    @ConfigEntry.Gui.CollapsibleObject
    public PathfindingSettings PATHFINDING_OPTIMIZATIONS = new PathfindingSettings();

    public static class PathfindingSettings {
        public int PATH_CACHE_SIZE = 128;
        public int PATH_UPDATE_INTERVAL_TICKS = 10;
        public int PATH_UPDATE_DISTANCE = 32;
    }
    
    // Configurações para BlockTickOptimizer
    @ConfigEntry.Category("block_ticking")
    @ConfigEntry.Gui.CollapsibleObject
    public BlockTickSettings BLOCK_TICK_OPTIMIZATIONS = new BlockTickSettings();

    public static class BlockTickSettings {
        public boolean ENABLE_ON_DEMAND_TICKING = true;
        @ConfigEntry.BoundedDiscrete(min = 0, max = 2)
        public int HOPPER_OPTIMIZATION_LEVEL = 2; // 0=off, 1=reduced freq, 2=on-change
    }
    
    // Configurações para RedstoneOptimizer
    @ConfigEntry.Category("redstone")
    @ConfigEntry.Gui.CollapsibleObject
    public RedstoneSettings REDSTONE_OPTIMIZATIONS = new RedstoneSettings();

    public static class RedstoneSettings {
        public boolean ENABLE_SIGNAL_COMPRESSION = true;
        public int MAX_REDSTONE_UPDATES_PER_TICK = 1024;
    }
    
    // Configurações para ParticleOptimizer
    @ConfigEntry.Category("particles")
    @ConfigEntry.Gui.CollapsibleObject
    public ParticleSettings PARTICLE_OPTIMIZATIONS = new ParticleSettings();

    public static class ParticleSettings {
        public boolean ENABLE_PARTICLE_CULLING = true;
        public boolean ENABLE_PARTICLE_LOD = true;
        public double PARTICLE_LOD_DISTANCE_START = 32.0; // Distance where LOD starts
        public double PARTICLE_LOD_STEP_DISTANCE = 16.0; // Distance interval for next LOD level
        @ConfigEntry.BoundedDiscrete(min = 1, max = 5)
        public int MAX_PARTICLE_LOD_LEVELS = 3; 
        public double PARTICLE_CULLING_DISTANCE_SQ = 64.0 * 64.0; // Squared distance for culling (64 blocks)
        public int MAX_TOTAL_PARTICLES = 10000; // Limit total particles rendered/ticked
    }

    // Configurações para EntityTickOptimizer
    @ConfigEntry.Category("entity_ticking")
    @ConfigEntry.Gui.CollapsibleObject
    public EntityTickSettings ENTITY_TICK_OPTIMIZATIONS = new EntityTickSettings();

    public static class EntityTickSettings {
        public int ENTITY_FREEZE_DISTANCE = 48; // Distance to player for freezing
    }
    
    // Configurações para InventoryOptimizer
    @ConfigEntry.Category("inventory")
    @ConfigEntry.Gui.CollapsibleObject
    public InventorySettings INVENTORY_OPTIMIZATIONS = new InventorySettings();

    public static class InventorySettings {
        public boolean ENABLE_SLOT_CACHING = true;
    }
    
    // Configurações para ChunkSavingOptimizer
    @ConfigEntry.Category("chunk_saving")
    @ConfigEntry.Gui.CollapsibleObject
    public ChunkSavingSettings CHUNK_SAVING_OPTIMIZATIONS = new ChunkSavingSettings();

    public static class ChunkSavingSettings {
        public int CHUNK_SAVE_BUFFER_SIZE = 64; // How many chunks to save in one batch
        public boolean ENABLE_ASYNC_COMPRESSION = true; // Use async compression for saving
    }
    
    // Configurações para SoundOptimizer
    @ConfigEntry.Category("sound")
    @ConfigEntry.Gui.CollapsibleObject
    public SoundSettings SOUND_OPTIMIZATIONS = new SoundSettings();

    public static class SoundSettings {
        public boolean ENABLE_SOUND_CULLING = true;
        public int SOUND_CULLING_DISTANCE = 32;
    }
    
    // NOVAS Configurações para HudOptimizer
    @ConfigEntry.Category("hud_performance")
    @ConfigEntry.Gui.CollapsibleObject
    public HudPerformanceSettings HUD_OPTIMIZATIONS = new HudPerformanceSettings();

    public static class HudPerformanceSettings {
        public boolean ENABLE_HUD_CACHING = true; // General HUD elements and debug screen
        public boolean ENABLE_DIRTY_FLAG_OPTIMIZATION = true; // For health, food, armor bars, hotbar
        public boolean ENABLE_CENTRALIZED_TEXT_BATCHING = true; // Ensures consistent text buffer flushing
        @ConfigEntry.BoundedDiscrete(min = 1, max = 60)
        public int HUD_UPDATE_INTERVAL_TICKS = 5; // General HUD elements update frequency (e.g., F3 info)
    }

    // NOVAS Configurações para Text Rendering (para profiling/futuras extensões)
    @ConfigEntry.Category("text_rendering")
    @ConfigEntry.Gui.CollapsibleObject
    public TextRenderingSettings TEXT_RENDERING_OPTIMIZATIONS = new TextRenderingSettings();

    public static class TextRenderingSettings {
        public boolean ENABLE_TEXT_PROFILING = false; // Only for debug: adds profiler sections for text rendering
    }

    // NOVAS Configurações para Tooltip & Inventário
    @ConfigEntry.Category("tooltip_inventory")
    @ConfigEntry.Gui.CollapsibleObject
    public TooltipInventorySettings TOOLTIP_INVENTORY_OPTIMIZATIONS = new TooltipInventorySettings();

    public static class TooltipInventorySettings {
        public boolean ENABLE_TOOLTIP_CACHING = true; // Cache TooltipComponent lists
        public boolean DISABLE_TOOLTIP_GRADIENTS = false; // Replace gradient backgrounds with solid color
    }


    // Configurações para ClientTerrainOptimizer (existente, mas com a estrutura Cloth Config)
    @ConfigEntry.Category("client_terrain")
    @ConfigEntry.Gui.CollapsibleObject
    public ClientTerrainSettings CLIENT_TERRAIN_OPTIMIZATIONS = new ClientTerrainSettings();

    public static class ClientTerrainSettings {
        public boolean ENABLE_TERRAIN_STREAMING = true;
        public boolean ENABLE_DIRECTIONAL_PRELOADING = true;
        public boolean ENABLE_CHUNK_LOD = true;
        public int CHUNK_LOD_DISTANCE_LEVEL1 = 48; // Distância para LOD level 1
        public int CHUNK_LOD_DISTANCE_LEVEL2 = 96; // Distância para LOD level 2
        @ConfigEntry.BoundedDiscrete(min = 1, max = 60)
        public int CHUNK_UPDATE_INTERVAL_LOD1 = 5; // Re-meshing a cada 5 ticks
        @ConfigEntry.BoundedDiscrete(min = 1, max = 60)
        public int CHUNK_UPDATE_INTERVAL_LOD2 = 10; // Re-meshing a cada 10 ticks
        public double MOVEMENT_ALIGNMENT_THRESHOLD = 0.6; // Alinhamento para considerar "na frente" do jogador
    }

    // Singleton instance para AutoConfig
    private static BariumConfig instance;

    /**
     * Retorna a instância singleton da configuração.
     * Garante que AutoConfig já foi registrado.
     * @return A instância de BariumConfig.
     */
    public static BariumConfig getInstance() {
        if (instance == null) {
            // Se ainda não foi inicializado, tente obter a instância do AutoConfig.
            // Isso assume que `AutoConfig.register` já foi chamado no ModInitializer.
            try {
                instance = AutoConfig.getConfigHolder(BariumConfig.class).getConfig();
            } catch (Exception e) {
                // Em um ambiente de desenvolvimento ou teste, pode ser necessário inicializar manualmente
                // Se o AutoConfig ainda não foi configurado.
                instance = new BariumConfig(); // Fallback para uma instância padrão
                BariumMod.LOGGER.error("BariumConfig not yet registered with AutoConfig, falling back to default instance. Error: " + e.getMessage());
            }
        }
        return instance;
    }

    /**
     * Registra as configurações do AutoConfig.
     * Deve ser chamado no ModInitializer.
     */
    public static void registerConfigs() {
        AutoConfig.register(BariumConfig.class, JanksonConfigSerializer::new);
    }
}