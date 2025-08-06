package com.barium.client.config;

import com.barium.config.BariumConfig;
import com.barium.config.ConfigData;
import com.barium.config.ConfigManager;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.text.Text;

public class BariumModMenu implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> {
            ConfigBuilder builder = ConfigBuilder.create()
                    .setParentScreen(parent)
                    .setTitle(Text.translatable("title.barium.config")); // Usa o título "Barium Settings"

            builder.setSavingRunnable(ConfigManager::saveConfig);

            ConfigData defaults = new ConfigData();
            ConfigEntryBuilder entryBuilder = builder.entryBuilder();
            
            // Categoria: Performance de Chunks
            ConfigCategory chunkPerformance = builder.getOrCreateCategory(Text.translatable("category.barium.chunk_performance"));
            
            chunkPerformance.addEntry(entryBuilder.startBooleanToggle(Text.translatable("option.barium.enable_directional_chunk_loading"), BariumConfig.C.ENABLE_DIRECTIONAL_CHUNK_LOADING)
                    .setDefaultValue(defaults.ENABLE_DIRECTIONAL_CHUNK_LOADING).setTooltip(Text.translatable("tooltip.barium.enable_directional_chunk_loading")).setSaveConsumer(newValue -> BariumConfig.C.ENABLE_DIRECTIONAL_CHUNK_LOADING = newValue).build());
            
            chunkPerformance.addEntry(entryBuilder.startBooleanToggle(Text.translatable("option.barium.enable_chunk_update_throttling"), BariumConfig.C.ENABLE_CHUNK_UPDATE_THROTTLING)
                    .setDefaultValue(defaults.ENABLE_CHUNK_UPDATE_THROTTLING).setTooltip(Text.translatable("tooltip.barium.enable_chunk_update_throttling")).setSaveConsumer(newValue -> BariumConfig.C.ENABLE_CHUNK_UPDATE_THROTTLING = newValue).build());

            chunkPerformance.addEntry(entryBuilder.startIntSlider(Text.translatable("option.barium.max_chunk_uploads"), BariumConfig.C.MAX_CHUNK_UPLOADS_PER_FRAME, 1, 16)
                    .setDefaultValue(defaults.MAX_CHUNK_UPLOADS_PER_FRAME).setTooltip(Text.translatable("tooltip.barium.max_chunk_uploads")).setSaveConsumer(newValue -> BariumConfig.C.MAX_CHUNK_UPLOADS_PER_FRAME = newValue).build());

            // Categoria: Otimização e LOD
            ConfigCategory cullingLod = builder.getOrCreateCategory(Text.translatable("category.barium.culling_lod"));

            cullingLod.addEntry(entryBuilder.startBooleanToggle(Text.translatable("option.barium.enable_entity_culling"), BariumConfig.C.ENABLE_ENTITY_CULLING)
                    .setDefaultValue(defaults.ENABLE_ENTITY_Culling).setTooltip(Text.translatable("tooltip.barium.enable_entity_culling")).setSaveConsumer(newValue -> BariumConfig.C.ENABLE_ENTITY_CULLING = newValue).build());
            
            cullingLod.addEntry(entryBuilder.startBooleanToggle(Text.translatable("option.barium.disable_texture_animations"), BariumConfig.C.DISABLE_TEXTURE_ANIMATIONS)
                    .setDefaultValue(defaults.DISABLE_TEXTURE_ANIMATIONS).setTooltip(Text.translatable("tooltip.barium.disable_texture_animations")).setSaveConsumer(newValue -> BariumConfig.C.DISABLE_TEXTURE_ANIMATIONS = newValue).build());

            return builder.build();
        };
    }
}