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
import net.minecraft.util.Formatting;

public class BariumModMenu implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> {
            ConfigBuilder builder = ConfigBuilder.create()
                    .setParentScreen(parent)
                    .setTitle(Text.translatable("title.barium.config"))
                    .setTransparentBackground(true);

            builder.setSavingRunnable(ConfigManager::saveConfig);

            ConfigData defaults = new ConfigData();
            ConfigEntryBuilder entryBuilder = builder.entryBuilder();
            
            // Categoria única para um layout mais limpo
            ConfigCategory mainCategory = builder.getOrCreateCategory(Text.translatable("category.barium.main"));

            // --- Subtítulo: Desempenho de Chunks ---
            mainCategory.addEntry(entryBuilder.startTextDescription(Text.translatable("category.barium.chunk_performance").formatted(Formatting.YELLOW)).build());
            
            mainCategory.addEntry(entryBuilder.startBooleanToggle(Text.translatable("option.barium.enable_frustum_culling"), BariumConfig.C.ENABLE_FRUSTUM_CHUNK_CULLING)
                    .setDefaultValue(defaults.ENABLE_FRUSTUM_CHUNK_CULLING).setTooltip(Text.translatable("tooltip.barium.enable_frustum_culling")).setSaveConsumer(v -> BariumConfig.C.ENABLE_FRUSTUM_CHUNK_CULLING = v).build());
            mainCategory.addEntry(entryBuilder.startBooleanToggle(Text.translatable("option.barium.enable_flood_fill_culling"), BariumConfig.C.ENABLE_FLOOD_FILL_CULLING)
                    .setDefaultValue(defaults.ENABLE_FLOOD_FILL_CULLING).setTooltip(Text.translatable("tooltip.barium.enable_flood_fill_culling")).setSaveConsumer(v -> BariumConfig.C.ENABLE_FLOOD_FILL_CULLING = v).build());
            mainCategory.addEntry(entryBuilder.startBooleanToggle(Text.translatable("option.barium.enable_occlusion_culling"), BariumConfig.C.ENABLE_OCCLUSION_CULLING)
                    .setDefaultValue(defaults.ENABLE_OCCLUSION_CULLING).setTooltip(Text.translatable("tooltip.barium.enable_occlusion_culling")).setSaveConsumer(v -> BariumConfig.C.ENABLE_OCCLUSION_CULLING = v).build());
            mainCategory.addEntry(entryBuilder.startBooleanToggle(Text.translatable("option.barium.cull_empty_sections"), BariumConfig.C.ENABLE_EMPTY_CHUNK_SECTION_CULLING)
                    .setDefaultValue(defaults.ENABLE_EMPTY_CHUNK_SECTION_CULLING).setTooltip(Text.translatable("tooltip.barium.cull_empty_sections")).setSaveConsumer(v -> BariumConfig.C.ENABLE_EMPTY_CHUNK_SECTION_CULLING = v).build());
            mainCategory.addEntry(entryBuilder.startBooleanToggle(Text.translatable("option.barium.enable_chunk_update_throttling"), BariumConfig.C.ENABLE_CHUNK_UPDATE_THROTTLING)
                    .setDefaultValue(defaults.ENABLE_CHUNK_UPDATE_THROTTLING).setTooltip(Text.translatable("tooltip.barium.enable_chunk_update_throttling")).setSaveConsumer(v -> BariumConfig.C.ENABLE_CHUNK_UPDATE_THROTTLING = v).build());
            mainCategory.addEntry(entryBuilder.startIntSlider(Text.translatable("option.barium.max_chunk_uploads"), BariumConfig.C.MAX_CHUNK_UPLOADS_PER_FRAME, 1, 16)
                    .setDefaultValue(defaults.MAX_CHUNK_UPLOADS_PER_FRAME).setTooltip(Text.translatable("tooltip.barium.max_chunk_uploads")).setSaveConsumer(v -> BariumConfig.C.MAX_CHUNK_UPLOADS_PER_FRAME = v).build());

            mainCategory.addEntry(entryBuilder.startTextDescription(Text.literal(" ")).build());

            // --- Subtítulo: Otimização e LOD ---
            mainCategory.addEntry(entryBuilder.startTextDescription(Text.translatable("category.barium.culling_lod").formatted(Formatting.YELLOW)).build());

            mainCategory.addEntry(entryBuilder.startBooleanToggle(Text.translatable("option.barium.enable_entity_culling"), BariumConfig.C.ENABLE_ENTITY_CULLING)
                    .setDefaultValue(defaults.ENABLE_ENTITY_CULLING).setTooltip(Text.translatable("tooltip.barium.enable_entity_culling")).setSaveConsumer(v -> BariumConfig.C.ENABLE_ENTITY_CULLING = v).build());
            mainCategory.addEntry(entryBuilder.startBooleanToggle(Text.translatable("option.barium.enable_block_entity_culling"), BariumConfig.C.ENABLE_BLOCK_ENTITY_CULLING)
                    .setDefaultValue(defaults.ENABLE_BLOCK_ENTITY_CULLING).setTooltip(Text.translatable("tooltip.barium.enable_block_entity_culling")).setSaveConsumer(v -> BariumConfig.C.ENABLE_BLOCK_ENTITY_CULLING = v).build());
            mainCategory.addEntry(entryBuilder.startBooleanToggle(Text.translatable("option.barium.enable_block_entity_occlusion_culling"), BariumConfig.C.ENABLE_BLOCK_ENTITY_OCCLUSION_CULLING)
                    .setDefaultValue(defaults.ENABLE_BLOCK_ENTITY_OCCLUSION_CULLING).setTooltip(Text.translatable("tooltip.barium.enable_block_entity_occlusion_culling")).setSaveConsumer(v -> BariumConfig.C.ENABLE_BLOCK_ENTITY_OCCLUSION_CULLING = v).build());
            mainCategory.addEntry(entryBuilder.startBooleanToggle(Text.translatable("option.barium.enable_beacon_optimization"), BariumConfig.C.ENABLE_BEACON_BEAM_OPTIMIZATION)
                    .setDefaultValue(defaults.ENABLE_BEACON_BEAM_OPTIMIZATION).setTooltip(Text.translatable("tooltip.barium.enable_beacon_optimization")).setSaveConsumer(v -> BariumConfig.C.ENABLE_BEACON_BEAM_OPTIMIZATION = v).build());

            mainCategory.addEntry(entryBuilder.startTextDescription(Text.literal(" ")).build());

            // --- Subtítulo: Partículas ---
            mainCategory.addEntry(entryBuilder.startTextDescription(Text.translatable("category.barium.particles").formatted(Formatting.YELLOW)).build());
            
            mainCategory.addEntry(entryBuilder.startBooleanToggle(Text.translatable("option.barium.enable_particle_optimizations"), BariumConfig.C.ENABLE_PARTICLE_OPTIMIZATION)
                    .setDefaultValue(defaults.ENABLE_PARTICLE_OPTIMIZATION).setTooltip(Text.translatable("tooltip.barium.enable_particle_optimizations")).setSaveConsumer(v -> BariumConfig.C.ENABLE_PARTICLE_OPTIMIZATION = v).build());
            mainCategory.addEntry(entryBuilder.startBooleanToggle(Text.translatable("option.barium.enable_particle_frustum_culling"), BariumConfig.C.ENABLE_PARTICLE_FRUSTUM_CULLING)
                    .setDefaultValue(defaults.ENABLE_PARTICLE_FRUSTUM_CULLING).setTooltip(Text.translatable("tooltip.barium.enable_particle_frustum_culling")).setSaveConsumer(v -> BariumConfig.C.ENABLE_PARTICLE_FRUSTUM_CULLING = v).build());
            mainCategory.addEntry(entryBuilder.startBooleanToggle(Text.translatable("option.barium.reduce_explosion_particles"), BariumConfig.C.ENABLE_EXPLOSION_PARTICLE_REDUCTION)
                    .setDefaultValue(defaults.ENABLE_EXPLOSION_PARTICLE_REDUCTION).setTooltip(Text.translatable("tooltip.barium.reduce_explosion_particles")).setSaveConsumer(v -> BariumConfig.C.ENABLE_EXPLOSION_PARTICLE_REDUCTION = v).build());
            mainCategory.addEntry(entryBuilder.startBooleanToggle(Text.translatable("option.barium.enable_global_particle_limit"), BariumConfig.C.ENABLE_GLOBAL_PARTICLE_LIMIT)
                .setDefaultValue(defaults.ENABLE_GLOBAL_PARTICLE_LIMIT).setTooltip(Text.translatable("tooltip.barium.enable_global_particle_limit")).setSaveConsumer(v -> BariumConfig.C.ENABLE_GLOBAL_PARTICLE_LIMIT = v).build());
            mainCategory.addEntry(entryBuilder.startIntSlider(Text.translatable("option.barium.max_global_particles"), BariumConfig.C.MAX_GLOBAL_PARTICLES, 512, 16384)
                .setDefaultValue(defaults.MAX_GLOBAL_PARTICLES).setTooltip(Text.translatable("tooltip.barium.max_global_particles")).setSaveConsumer(v -> BariumConfig.C.MAX_GLOBAL_PARTICLES = v).build());

            mainCategory.addEntry(entryBuilder.startTextDescription(Text.literal(" ")).build());

            // --- Subtítulo: Visuais e Lógica ---
            mainCategory.addEntry(entryBuilder.startTextDescription(Text.translatable("category.barium.game_logic").formatted(Formatting.YELLOW)).build());

            mainCategory.addEntry(entryBuilder.startBooleanToggle(Text.translatable("option.barium.disable_texture_animations"), BariumConfig.C.DISABLE_TEXTURE_ANIMATIONS)
                    .setDefaultValue(defaults.DISABLE_TEXTURE_ANIMATIONS).setTooltip(Text.translatable("tooltip.barium.disable_texture_animations")).setSaveConsumer(v -> BariumConfig.C.DISABLE_TEXTURE_ANIMATIONS = v).build());
            mainCategory.addEntry(entryBuilder.startBooleanToggle(Text.translatable("option.barium.enable_llvmpipe_mode"), BariumConfig.C.ENABLE_LLVMPIPE_MODE)
                    .setDefaultValue(defaults.ENABLE_LLVMPIPE_MODE).setTooltip(Text.translatable("tooltip.barium.enable_llvmpipe_mode")).setSaveConsumer(v -> BariumConfig.C.ENABLE_LLVMPIPE_MODE = v).build());
            mainCategory.addEntry(entryBuilder.startIntSlider(Text.translatable("option.barium.mipmap_level_override"), BariumConfig.C.MIPMAP_LEVEL_OVERRIDE, 0, 4)
                    .setDefaultValue(defaults.MIPMAP_LEVEL_OVERRIDE).setTooltip(Text.translatable("tooltip.barium.mipmap_level_override")).setSaveConsumer(v -> BariumConfig.C.MIPMAP_LEVEL_OVERRIDE = v).build());
            mainCategory.addEntry(entryBuilder.startBooleanToggle(Text.translatable("option.barium.cache_debug_hud"), BariumConfig.C.CACHE_DEBUG_HUD)
                    .setDefaultValue(defaults.CACHE_DEBUG_HUD).setTooltip(Text.translatable("tooltip.barium.cache_debug_hud")).setSaveConsumer(v -> BariumConfig.C.CACHE_DEBUG_HUD = v).build());
            mainCategory.addEntry(entryBuilder.startBooleanToggle(Text.translatable("option.barium.enable_tooltip_caching"), BariumConfig.C.ENABLE_TOOLTIP_CACHING)
                    .setDefaultValue(defaults.ENABLE_TOOLTIP_CACHING).setTooltip(Text.translatable("tooltip.barium.enable_tooltip_caching")).setSaveConsumer(v -> BariumConfig.C.ENABLE_TOOLTIP_CACHING = v).build());
            mainCategory.addEntry(entryBuilder.startBooleanToggle(Text.translatable("option.barium.disable_entity_outlines"), BariumConfig.C.DISABLE_ENTITY_OUTLINES)
                    .setDefaultValue(defaults.DISABLE_ENTITY_OUTLINES).setTooltip(Text.translatable("tooltip.barium.disable_entity_outlines")).setSaveConsumer(v -> BariumConfig.C.DISABLE_ENTITY_OUTLINES = v).build());
            mainCategory.addEntry(entryBuilder.startBooleanToggle(Text.translatable("option.barium.enable_half_res_outlines"), BariumConfig.C.ENABLE_HALF_RESOLUTION_ENTITY_OUTLINES)
                    .setDefaultValue(defaults.ENABLE_HALF_RESOLUTION_ENTITY_OUTLINES).setTooltip(Text.translatable("tooltip.barium.enable_half_res_outlines")).setSaveConsumer(v -> BariumConfig.C.ENABLE_HALF_RESOLUTION_ENTITY_OUTLINES = v).build());
            mainCategory.addEntry(entryBuilder.startBooleanToggle(Text.translatable("option.barium.enable_entity_tick_culling"), BariumConfig.C.ENABLE_ENTITY_TICK_CULLING)
                    .setDefaultValue(defaults.ENABLE_ENTITY_TICK_CULLING).setTooltip(Text.translatable("tooltip.barium.enable_entity_tick_culling")).setSaveConsumer(v -> BariumConfig.C.ENABLE_ENTITY_TICK_CULLING = v).build());
            mainCategory.addEntry(entryBuilder.startBooleanToggle(Text.translatable("option.barium.reduce_ambient_particles"), BariumConfig.C.REDUCE_AMBIENT_PARTICLES)
                    .setDefaultValue(defaults.REDUCE_AMBIENT_PARTICLES).setTooltip(Text.translatable("tooltip.barium.reduce_ambient_particles")).setSaveConsumer(v -> BariumConfig.C.REDUCE_AMBIENT_PARTICLES = v).build());
            mainCategory.addEntry(entryBuilder.startBooleanToggle(Text.translatable("option.barium.enable_hopper_culling"), BariumConfig.C.ENABLE_HOPPER_TICK_CULLING)
                    .setDefaultValue(defaults.ENABLE_HOPPER_TICK_CULLING).setTooltip(Text.translatable("tooltip.barium.enable_hopper_culling")).setSaveConsumer(v -> BariumConfig.C.ENABLE_HOPPER_TICK_CULLING = v).build());

            return builder.build();
        };
    }
}