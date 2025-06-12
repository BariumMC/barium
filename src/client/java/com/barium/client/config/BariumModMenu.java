// --- Substitua o conteúdo em: src/client/java/com/barium/client/config/BariumModMenu.java ---
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
                    .setTitle(Text.translatable("title.barium.config"));

            builder.setSavingRunnable(ConfigManager::saveConfig);

            ConfigData defaults = new ConfigData();
            ConfigEntryBuilder entryBuilder = builder.entryBuilder();

            // --- Categoria Chunk Performance (Existente) ---
            ConfigCategory chunkPerformance = builder.getOrCreateCategory(Text.translatable("category.barium.chunk_performance"));
            chunkPerformance.addEntry(entryBuilder.startBooleanToggle(Text.translatable("option.barium.cull_empty_sections"), BariumConfig.C.ENABLE_EMPTY_CHUNK_SECTION_CULLING)
                    .setDefaultValue(defaults.ENABLE_EMPTY_CHUNK_SECTION_CULLING)
                    .setTooltip(Text.translatable("tooltip.barium.cull_empty_sections"))
                    .setSaveConsumer(newValue -> BariumConfig.C.ENABLE_EMPTY_CHUNK_SECTION_CULLING = newValue)
                    .build());
            // ... (outras opções existentes) ...

            // --- NOVO: Categoria para as novas otimizações ---
            ConfigCategory gameLogic = builder.getOrCreateCategory(Text.translatable("category.barium.game_logic"));

            gameLogic.addEntry(entryBuilder.startBooleanToggle(Text.translatable("option.barium.enable_entity_tick_culling"), BariumConfig.C.ENABLE_ENTITY_TICK_CULLING)
                    .setDefaultValue(defaults.ENABLE_ENTITY_TICK_CULLING)
                    .setTooltip(Text.translatable("tooltip.barium.enable_entity_tick_culling"))
                    .setSaveConsumer(newValue -> BariumConfig.C.ENABLE_ENTITY_TICK_CULLING = newValue)
                    .build());
            
            gameLogic.addEntry(entryBuilder.startIntSlider(Text.translatable("option.barium.entity_tick_distance"), (int) Math.sqrt(BariumConfig.C.ENTITY_TICK_CULLING_DISTANCE_SQ), 16, 128)
                    .setDefaultValue((int) Math.sqrt(defaults.ENTITY_TICK_CULLING_DISTANCE_SQ))
                    .setTooltip(Text.translatable("tooltip.barium.entity_tick_distance"))
                    .setText(value -> Text.translatable("generic.barium.distance_blocks", value))
                    .setSaveConsumer(newValue -> BariumConfig.C.ENTITY_TICK_CULLING_DISTANCE_SQ = newValue * newValue)
                    .build());

            gameLogic.addEntry(entryBuilder.startBooleanToggle(Text.translatable("option.barium.reduce_ambient_particles"), BariumConfig.C.REDUCE_AMBIENT_PARTICLES)
                    .setDefaultValue(defaults.REDUCE_AMBIENT_PARTICLES)
                    .setTooltip(Text.translatable("tooltip.barium.reduce_ambient_particles"))
                    .setSaveConsumer(newValue -> BariumConfig.C.REDUCE_AMBIENT_PARTICLES = newValue)
                    .build());
            
            // ... (código para as outras categorias existentes) ...

            return builder.build();
        };
    }
}