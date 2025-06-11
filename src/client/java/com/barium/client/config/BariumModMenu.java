package com.barium.client.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class BariumModMenu implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> {
            ConfigBuilder builder = ConfigBuilder.create()
                    .setParentScreen(parent)
                    .setTitle(Text.translatable("title.barium.config")); // MUDANÇA AQUI

            builder.setSavingRunnable(ConfigManager::saveConfig);

            ConfigEntryBuilder entryBuilder = builder.entryBuilder();

            // --- Categoria: Chunk Performance ---
            ConfigCategory chunkPerformance = builder.getOrCreateCategory(Text.translatable("category.barium.chunk_performance")); // MUDANÇA AQUI

            chunkPerformance.addEntry(entryBuilder.startBooleanToggle(Text.translatable("option.barium.cull_empty_sections"), BariumConfig.C.ENABLE_EMPTY_CHUNK_SECTION_CULLING)
                    .setDefaultValue(new ConfigData().ENABLE_EMPTY_CHUNK_SECTION_CULLING)
                    .setTooltip(Text.translatable("tooltip.barium.cull_empty_sections")) // MUDANÇA AQUI
                    .setSaveConsumer(newValue -> BariumConfig.C.ENABLE_EMPTY_CHUNK_SECTION_CULLING = newValue)
                    .build());

            chunkPerformance.addEntry(entryBuilder.startBooleanToggle(Text.translatable("option.barium.enable_chunk_update_throttling"), BariumConfig.C.ENABLE_CHUNK_UPDATE_THROTTLING)
                    .setDefaultValue(new ConfigData().ENABLE_CHUNK_UPDATE_THROTTLING)
                    .setTooltip(Text.translatable("tooltip.barium.enable_chunk_update_throttling")) // MUDANÇA AQUI
                    .setSaveConsumer(newValue -> BariumConfig.C.ENABLE_CHUNK_UPDATE_THROTTLING = newValue)
                    .build());

            chunkPerformance.addEntry(entryBuilder.startIntSlider(Text.translatable("option.barium.max_chunk_uploads"), BariumConfig.C.MAX_CHUNK_UPLOADS_PER_FRAME, 1, 16)
                    .setDefaultValue(new ConfigData().MAX_CHUNK_UPLOADS_PER_FRAME)
                    .setTooltip(Text.translatable("tooltip.barium.max_chunk_uploads")) // MUDANÇA AQUI
                    .setSaveConsumer(newValue -> BariumConfig.C.MAX_CHUNK_UPLOADS_PER_FRAME = newValue)
                    .build());

            // --- Categoria: Culling & LOD ---
            ConfigCategory culling = builder.getOrCreateCategory(Text.translatable("category.barium.culling_lod")); // MUDANÇA AQUI
            
            culling.addEntry(entryBuilder.startBooleanToggle(Text.translatable("option.barium.enable_block_entity_culling"), BariumConfig.C.ENABLE_BLOCK_ENTITY_CULLING)
                    .setDefaultValue(new ConfigData().ENABLE_BLOCK_ENTITY_CULLING)
                    .setSaveConsumer(newValue -> BariumConfig.C.ENABLE_BLOCK_ENTITY_CULLING = newValue)
                    .build());
            
            culling.addEntry(entryBuilder.startIntSlider(Text.translatable("option.barium.block_entity_render_distance"), (int) Math.sqrt(BariumConfig.C.MAX_BLOCK_ENTITY_RENDER_DISTANCE_SQ), 16, 256)
                    .setDefaultValue((int) Math.sqrt(new ConfigData().MAX_BLOCK_ENTITY_RENDER_DISTANCE_SQ))
                    .setSaveConsumer(newValue -> BariumConfig.C.MAX_BLOCK_ENTITY_RENDER_DISTANCE_SQ = newValue * newValue)
                    .build());
            
            culling.addEntry(entryBuilder.startBooleanToggle(Text.translatable("option.barium.enable_dense_foliage_culling"), BariumConfig.C.ENABLE_DENSE_FOLIAGE_CULLING)
                    .setDefaultValue(new ConfigData().ENABLE_DENSE_FOLIAGE_CULLING)
                    .setSaveConsumer(newValue -> BariumConfig.C.ENABLE_DENSE_FOLIAGE_CULLING = newValue)
                    .build());

            culling.addEntry(entryBuilder.startIntSlider(Text.translatable("option.barium.dense_foliage_culling_level"), BariumConfig.C.DENSE_FOLIAGE_CULLING_LEVEL, 0, 3)
                    .setDefaultValue(new ConfigData().DENSE_FOLIAGE_CULLING_LEVEL)
                    .setSaveConsumer(newValue -> BariumConfig.C.DENSE_FOLIAGE_CULLING_LEVEL = newValue)
                    .build());
            
            // --- Categoria: Partículas ---
            ConfigCategory particles = builder.getOrCreateCategory(Text.translatable("category.barium.particles")); // MUDANÇA AQUI

            particles.addEntry(entryBuilder.startBooleanToggle(Text.translatable("option.barium.enable_particle_optimizations"), BariumConfig.C.ENABLE_PARTICLE_OPTIMIZATION)
                    .setDefaultValue(new ConfigData().ENABLE_PARTICLE_OPTIMIZATION)
                    .setSaveConsumer(newValue -> BariumConfig.C.ENABLE_PARTICLE_OPTIMIZATION = newValue)
                    .build());
            
            particles.addEntry(entryBuilder.startIntSlider(Text.translatable("option.barium.max_global_particles"), BariumConfig.C.MAX_GLOBAL_PARTICLES, 0, 10000)
                    .setDefaultValue(new ConfigData().MAX_GLOBAL_PARTICLES)
                    .setSaveConsumer(newValue -> BariumConfig.C.MAX_GLOBAL_PARTICLES = newValue)
                    .build());

            return builder.build();
        };
    }
}