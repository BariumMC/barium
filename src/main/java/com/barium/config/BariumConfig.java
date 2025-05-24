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

    @ConfigEntry.Category("rendering")
    @ConfigEntry.Gui.TransitiveObject
    public RenderConfig render = new RenderConfig();

    @ConfigEntry.Category("chunk_building")
    @ConfigEntry.Gui.TransitiveObject
    public ChunkBuildingConfig chunkBuilding = new ChunkBuildingConfig();

    public static class RenderConfig implements ConfigData {
        // CORRIGIDO: Removendo 'value ='
        @ConfigEntry.Gui.Tooltip("barium.config.tooltip.aggressiveFaceCulling")
        public boolean aggressiveFaceCulling = true; // Potentially removes more hidden faces (risks minor visual artifacts)

        // CORRIGIDO: Removendo 'value ='
        @ConfigEntry.Gui.Tooltip("barium.config.tooltip.optimizeFluidRendering")
        public boolean optimizeFluidRendering = true; // Specialized fluid rendering optimization
    }

    public static class ChunkBuildingConfig implements ConfigData {
        // CORRIGIDO: Removendo 'value ='
        @ConfigEntry.Gui.Tooltip("barium.config.tooltip.chunkBuilderThreads")
        @ConfigEntry.BoundedDiscrete(min = 1, max = 8) // Common range for CPU cores
        public int chunkBuilderThreads = Math.max(1, Runtime.getRuntime().availableProcessors() / 2); // Half available processors, min 1
    
        // CORRIGIDO: Removendo 'value ='
        @ConfigEntry.Gui.Tooltip("barium.config.tooltip.enableQuadSorting")
        public boolean enableQuadSorting = false; // Desativado por padrão devido à complexidade da implementação
    }

    public static void registerConfigs() {
        AutoConfig.register(BariumConfig.class, JanksonConfigSerializer::new);
    }

    public static BariumConfig get() {
        return AutoConfig.getConfigHolder(BariumConfig.class).getConfig();
    }
}