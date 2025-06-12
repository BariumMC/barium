package com.barium.client.config;

import com.barium.config.BariumConfig;
import com.barium.config.ConfigData;
import com.barium.config.ConfigManager;
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
            // Cria o construtor da tela de configuração
            ConfigBuilder builder = ConfigBuilder.create()
                    .setParentScreen(parent)
                    .setTitle(Text.translatable("title.barium.config"));

            // Define a ação a ser executada ao salvar (chama nosso ConfigManager)
            builder.setSavingRunnable(ConfigManager::saveConfig);

            // Cria uma instância de ConfigData para obter os valores padrão
            ConfigData defaults = new ConfigData();
            // Pega o construtor de entradas para criar toggles, sliders, etc.
            ConfigEntryBuilder entryBuilder = builder.entryBuilder();

            // --- Categoria 1: Desempenho de Chunks ---
            ConfigCategory chunkPerformance = builder.getOrCreateCategory(Text.translatable("category.barium.chunk_performance"));

            chunkPerformance.addEntry(entryBuilder.startBooleanToggle(Text.translatable("option.barium.cull_empty_sections"), BariumConfig.C.ENABLE_EMPTY_CHUNK_SECTION_CULLING)
                    .setDefaultValue(defaults.ENABLE_EMPTY_CHUNK_SECTION_CULLING)
                    .setTooltip(Text.translatable("tooltip.barium.cull_empty_sections"))
                    .setSaveConsumer(newValue -> BariumConfig.C.ENABLE_EMPTY_CHUNK_SECTION_CULLING = newValue)
                    .build());

            chunkPerformance.addEntry(entryBuilder.startBooleanToggle(Text.translatable("option.barium.enable_chunk_update_throttling"), BariumConfig.C.ENABLE_CHUNK_UPDATE_THROTTLING)
                    .setDefaultValue(defaults.ENABLE_CHUNK_UPDATE_THROTTLING)
                    .setTooltip(Text.translatable("tooltip.barium.enable_chunk_update_throttling"))
                    .setSaveConsumer(newValue -> BariumConfig.C.ENABLE_CHUNK_UPDATE_THROTTLING = newValue)
                    .build());

            chunkPerformance.addEntry(entryBuilder.startIntSlider(Text.translatable("option.barium.max_chunk_uploads"), BariumConfig.C.MAX_CHUNK_UPLOADS_PER_FRAME, 1, 16)
                    .setDefaultValue(defaults.MAX_CHUNK_UPLOADS_PER_FRAME)
                    .setTooltip(Text.translatable("tooltip.barium.max_chunk_uploads"))
                    .setSaveConsumer(newValue -> BariumConfig.C.MAX_CHUNK_UPLOADS_PER_FRAME = newValue)
                    .build());

            // --- Categoria 2: Otimização e LOD (Level of Detail) ---
            ConfigCategory cullingLod = builder.getOrCreateCategory(Text.translatable("category.barium.culling_lod"));

            cullingLod.addEntry(entryBuilder.startBooleanToggle(Text.translatable("option.barium.enable_block_entity_culling"), BariumConfig.C.ENABLE_BLOCK_ENTITY_CULLING)
                    .setDefaultValue(defaults.ENABLE_BLOCK_ENTITY_CULLING)
                    .setSaveConsumer(newValue -> BariumConfig.C.ENABLE_BLOCK_ENTITY_CULLING = newValue)
                    .build());

            cullingLod.addEntry(entryBuilder.startIntSlider(Text.translatable("option.barium.block_entity_render_distance"), (int) Math.sqrt(BariumConfig.C.MAX_BLOCK_ENTITY_RENDER_DISTANCE_SQ), 16, 256)
                    .setDefaultValue((int) Math.sqrt(defaults.MAX_BLOCK_ENTITY_RENDER_DISTANCE_SQ))
                    .setSaveConsumer(newValue -> BariumConfig.C.MAX_BLOCK_ENTITY_RENDER_DISTANCE_SQ = newValue * newValue)
                    .build());

            cullingLod.addEntry(entryBuilder.startBooleanToggle(Text.translatable("option.barium.enable_dense_foliage_culling"), BariumConfig.C.ENABLE_DENSE_FOLIAGE_CULLING)
                    .setDefaultValue(defaults.ENABLE_DENSE_FOLIAGE_CULLING)
                    .setSaveConsumer(newValue -> BariumConfig.C.ENABLE_DENSE_FOLIAGE_CULLING = newValue)
                    .build());

            cullingLod.addEntry(entryBuilder.startIntSlider(Text.translatable("option.barium.dense_foliage_culling_level"), BariumConfig.C.DENSE_FOLIAGE_CULLING_LEVEL, 0, 3)
                    .setDefaultValue(defaults.DENSE_FOLIAGE_CULLING_LEVEL)
                    .setSaveConsumer(newValue -> BariumConfig.C.DENSE_FOLIAGE_CULLING_LEVEL = newValue)
                    .build());

            // --- Categoria 3: Partículas ---
            ConfigCategory particles = builder.getOrCreateCategory(Text.translatable("category.barium.particles"));

            particles.addEntry(entryBuilder.startBooleanToggle(Text.translatable("option.barium.enable_particle_optimizations"), BariumConfig.C.ENABLE_PARTICLE_OPTIMIZATION)
                    .setDefaultValue(defaults.ENABLE_PARTICLE_OPTIMIZATION)
                    .setSaveConsumer(newValue -> BariumConfig.C.ENABLE_PARTICLE_OPTIMIZATION = newValue)
                    .build());

            particles.addEntry(entryBuilder.startIntSlider(Text.translatable("option.barium.max_global_particles"), BariumConfig.C.MAX_GLOBAL_PARTICLES, 0, 10000)
                    .setDefaultValue(defaults.MAX_GLOBAL_PARTICLES)
                    .setSaveConsumer(newValue -> BariumConfig.C.MAX_GLOBAL_PARTICLES = newValue)
                    .build());

            // --- Categoria 4: Pós-Processamento ---
            ConfigCategory postProcessing = builder.getOrCreateCategory(Text.translatable("category.barium.post_processing"));

            postProcessing.addEntry(entryBuilder.startBooleanToggle(Text.translatable("option.barium.enable_half_res_outlines"), BariumConfig.C.ENABLE_HALF_RESOLUTION_ENTITY_OUTLINES)
                    .setDefaultValue(defaults.ENABLE_HALF_RESOLUTION_ENTITY_OUTLINES)
                    .setTooltip(Text.translatable("tooltip.barium.enable_half_res_outlines"))
                    .setSaveConsumer(newValue -> BariumConfig.C.ENABLE_HALF_RESOLUTION_ENTITY_OUTLINES = newValue)
                    .build());

            // --- Categoria 5: Lógica do Jogo / Ticks ---
            ConfigCategory gameLogic = builder.getOrCreateCategory(Text.translatable("category.barium.game_logic"));

            gameLogic.addEntry(entryBuilder.startBooleanToggle(Text.translatable("option.barium.enable_entity_tick_culling"), BariumConfig.C.ENABLE_ENTITY_TICK_CULLING)
                    .setDefaultValue(defaults.ENABLE_ENTITY_TICK_CULLING)
                    .setTooltip(Text.translatable("tooltip.barium.enable_entity_tick_culling"))
                    .setSaveConsumer(newValue -> BariumConfig.C.ENABLE_ENTITY_TICK_CULLING = newValue)
                    .build());

            gameLogic.addEntry(entryBuilder.startIntSlider(Text.translatable("option.barium.entity_tick_distance"), (int) Math.sqrt(BariumConfig.C.ENTITY_TICK_CULLING_DISTANCE_SQ), 16, 128)
                    .setDefaultValue((int) Math.sqrt(defaults.ENTITY_TICK_CULLING_DISTANCE_SQ))
                    .setTooltip(Text.translatable("tooltip.barium.entity_tick_distance"))
                    .setTextGetter(value -> Text.translatable("generic.barium.distance_blocks", value))
                    .setSaveConsumer(newValue -> BariumConfig.C.ENTITY_TICK_CULLING_DISTANCE_SQ = newValue * newValue)
                    .build());

            gameLogic.addEntry(entryBuilder.startBooleanToggle(Text.translatable("option.barium.reduce_ambient_particles"), BariumConfig.C.REDUCE_AMBIENT_PARTICLES)
                    .setDefaultValue(defaults.REDUCE_AMBIENT_PARTICLES)
                    .setTooltip(Text.translatable("tooltip.barium.reduce_ambient_particles"))
                    .setSaveConsumer(newValue -> BariumConfig.C.REDUCE_AMBIENT_PARTICLES = newValue)
                    .build());

            // --- Categoria 6: Otimizações Específicas ---
            ConfigCategory specificLogic = builder.getOrCreateCategory(Text.translatable("category.barium.specific_optimizations"));

            specificLogic.addEntry(entryBuilder.startBooleanToggle(Text.translatable("option.barium.enable_hopper_culling"), BariumConfig.C.ENABLE_HOPPER_TICK_CULLING)
                    .setDefaultValue(defaults.ENABLE_HOPPER_TICK_CULLING)
                    .setTooltip(Text.translatable("tooltip.barium.enable_hopper_culling"))
                    .setSaveConsumer(newValue -> BariumConfig.C.ENABLE_HOPPER_TICK_CULLING = newValue)
                    .build());
            
            specificLogic.addEntry(entryBuilder.startBooleanToggle(Text.translatable("option.barium.reduce_explosion_particles"), BariumConfig.C.ENABLE_EXPLOSION_PARTICLE_REDUCTION)
                    .setDefaultValue(defaults.ENABLE_EXPLOSION_PARTICLE_REDUCTION)
                    .setTooltip(Text.translatable("tooltip.barium.reduce_explosion_particles"))
                    .setSaveConsumer(newValue -> BariumConfig.C.ENABLE_EXPLOSION_PARTICLE_REDUCTION = newValue)
                    .build());

            specificLogic.addEntry(entryBuilder.startBooleanToggle(Text.translatable("option.barium.enable_beacon_culling"), BariumConfig.C.ENABLE_BEACON_BEAM_CULLING)
                    .setDefaultValue(defaults.ENABLE_BEACON_BEAM_CULLING)
                    .setTooltip(Text.translatable("tooltip.barium.enable_beacon_culling"))
                    .setSaveConsumer(newValue -> BariumConfig.C.ENABLE_BEACON_BEAM_CULLING = newValue)
                    .build());

            // Constrói e retorna a tela final
            return builder.build();
        };
    }
}