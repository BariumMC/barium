// src/main/java/com/barium/config/BariumConfig.java
package com.barium.config;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import com.barium.BariumMod;

@Config(name = "barium_optimization")
public class BariumConfig implements ConfigData {

    @ConfigEntry.Category("rendering")
    @ConfigEntry.Gui.TransitiveObject
    public RenderConfig render = new RenderConfig();

    @ConfigEntry.Category("chunk_building")
    @ConfigEntry.Gui.TransitiveObject
    public ChunkBuildingConfig chunkBuilding = new ChunkBuildingConfig();

    public static class RenderConfig implements ConfigData {
        // Sintaxe de array para Tooltip - esta DEVERIA funcionar com Cloth Config 18.x
        @ConfigEntry.Gui.Tooltip({"barium.config.tooltip.aggressiveFaceCulling"})
        public boolean aggressiveFaceCulling = true;

        // Sintaxe de array para Tooltip
        @ConfigEntry.Gui.Tooltip({"barium.config.tooltip.optimizeFluidRendering"})
        public boolean optimizeFluidRendering = true;
    }

    public static class ChunkBuildingConfig implements ConfigData {
        // Sintaxe de array para Tooltip
        @ConfigEntry.Gui.Tooltip({"barium.config.tooltip.chunkBuilderThreads"})
        @ConfigEntry.BoundedDiscrete(min = 1, max = 8)
        public int chunkBuilderThreads = Math.max(1, Runtime.getRuntime().availableProcessors() / 2);
    
        // Sintaxe de array para Tooltip
        @ConfigEntry.Gui.Tooltip({"barium.config.tooltip.enableQuadSorting"})
        public boolean enableQuadSorting = false;
    }

    public static void registerConfigs() {
        AutoConfig.register(BariumConfig.class, JanksonConfigSerializer::new);
    }

    public static BariumConfig get() {
        return AutoConfig.getConfigHolder(BariumConfig.class).getConfig();
    }
}