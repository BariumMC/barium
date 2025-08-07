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
            ConfigBuilder builder = ConfigBuilder.create()
                    .setParentScreen(parent)
                    .setTitle(Text.translatable("title.barium.config"))
                    .setTransparentBackground(true); // Deixa o fundo do mundo visível!

            // Salva as configurações quando o usuário clicar em "Salvar"
            builder.setSavingRunnable(ConfigManager::saveConfig);

            ConfigData defaults = new ConfigData();
            ConfigEntryBuilder entryBuilder = builder.entryBuilder();

            // Categoria principal para todas as opções
            ConfigCategory performance = builder.getOrCreateCategory(Text.translatable("category.barium.performance"));

            // --- Subtítulo para Otimizações de Chunk ---
            performance.addEntry(entryBuilder.startTextDescription(Text.translatable("category.barium.chunk_performance").formatted(net.minecraft.util.Formatting.YELLOW)).build());

            performance.addEntry(entryBuilder.startBooleanToggle(Text.translatable("option.barium.enable_frustum_culling"), BariumConfig.C.ENABLE_FRUSTUM_CHUNK_CULLING)
                    .setDefaultValue(defaults.ENABLE_FRUSTUM_CHUNK_CULLING).setTooltip(Text.translatable("tooltip.barium.enable_frustum_culling")).setSaveConsumer(v -> BariumConfig.C.ENABLE_FRUSTUM_CHUNK_CULLING = v).build());
            performance.addEntry(entryBuilder.startBooleanToggle(Text.translatable("option.barium.enable_flood_fill_culling"), BariumConfig.C.ENABLE_FLOOD_FILL_CULLING)
                    .setDefaultValue(defaults.ENABLE_FLOOD_FILL_CULLING).setTooltip(Text.translatable("tooltip.barium.enable_flood_fill_culling")).setSaveConsumer(v -> BariumConfig.C.ENABLE_FLOOD_FILL_CULLING = v).build());
            performance.addEntry(entryBuilder.startBooleanToggle(Text.translatable("option.barium.enable_occlusion_culling"), BariumConfig.C.ENABLE_OCCLUSION_CULLING)
                    .setDefaultValue(defaults.ENABLE_OCCLUSION_CULLING).setTooltip(Text.translatable("tooltip.barium.enable_occlusion_culling")).setSaveConsumer(v -> BariumConfig.C.ENABLE_OCCLUSION_CULLING = v).build());
            performance.addEntry(entryBuilder.startBooleanToggle(Text.translatable("option.barium.cull_empty_sections"), BariumConfig.C.ENABLE_EMPTY_CHUNK_SECTION_CULLING)
                    .setDefaultValue(defaults.ENABLE_EMPTY_CHUNK_SECTION_CULLING).setTooltip(Text.translatable("tooltip.barium.cull_empty_sections")).setSaveConsumer(v -> BariumConfig.C.ENABLE_EMPTY_CHUNK_SECTION_CULLING = v).build());
            
            // Adiciona um espaço em branco para separar visualmente
            performance.addEntry(entryBuilder.startTextDescription(Text.literal(" ")).build());

            // --- Subtítulo para Otimizações de Renderização ---
            performance.addEntry(entryBuilder.startTextDescription(Text.translatable("category.barium.culling_lod").formatted(net.minecraft.util.Formatting.YELLOW)).build());
            
            performance.addEntry(entryBuilder.startBooleanToggle(Text.translatable("option.barium.enable_entity_culling"), BariumConfig.C.ENABLE_ENTITY_CULLING)
                    .setDefaultValue(defaults.ENABLE_ENTITY_CULLING).setTooltip(Text.translatable("tooltip.barium.enable_entity_culling")).setSaveConsumer(v -> BariumConfig.C.ENABLE_ENTITY_CULLING = v).build());
            performance.addEntry(entryBuilder.startBooleanToggle(Text.translatable("option.barium.enable_block_entity_occlusion_culling"), BariumConfig.C.ENABLE_BLOCK_ENTITY_OCCLUSION_CULLING)
                    .setDefaultValue(defaults.ENABLE_BLOCK_ENTITY_OCCLUSION_CULLING).setTooltip(Text.translatable("tooltip.barium.enable_block_entity_occlusion_culling")).setSaveConsumer(v -> BariumConfig.C.ENABLE_BLOCK_ENTITY_OCCLUSION_CULLING = v).build());
            performance.addEntry(entryBuilder.startBooleanToggle(Text.translatable("option.barium.enable_beacon_optimization"), BariumConfig.C.ENABLE_BEACON_BEAM_OPTIMIZATION)
                    .setDefaultValue(defaults.ENABLE_BEACON_BEAM_OPTIMIZATION).setTooltip(Text.translatable("tooltip.barium.enable_beacon_optimization")).setSaveConsumer(v -> BariumConfig.C.ENABLE_BEACON_BEAM_OPTIMIZATION = v).build());
            
            performance.addEntry(entryBuilder.startTextDescription(Text.literal(" ")).build());

            // --- Subtítulo para Otimizações de Partículas ---
            performance.addEntry(entryBuilder.startTextDescription(Text.translatable("category.barium.particles").formatted(net.minecraft.util.Formatting.YELLOW)).build());

            performance.addEntry(entryBuilder.startBooleanToggle(Text.translatable("option.barium.enable_particle_optimizations"), BariumConfig.C.ENABLE_PARTICLE_OPTIMIZATION)
                    .setDefaultValue(defaults.ENABLE_PARTICLE_OPTIMIZATION).setTooltip(Text.translatable("tooltip.barium.enable_particle_optimizations")).setSaveConsumer(v -> BariumConfig.C.ENABLE_PARTICLE_OPTIMIZATION = v).build());
            performance.addEntry(entryBuilder.startBooleanToggle(Text.translatable("option.barium.reduce_explosion_particles"), BariumConfig.C.ENABLE_EXPLOSION_PARTICLE_REDUCTION)
                    .setDefaultValue(defaults.ENABLE_EXPLOSION_PARTICLE_REDUCTION).setTooltip(Text.translatable("tooltip.barium.reduce_explosion_particles")).setSaveConsumer(v -> BariumConfig.C.ENABLE_EXPLOSION_PARTICLE_REDUCTION = v).build());

            performance.addEntry(entryBuilder.startTextDescription(Text.literal(" ")).build());

            // --- Subtítulo para Otimizações Visuais e de Lógica ---
            performance.addEntry(entryBuilder.startTextDescription(Text.translatable("category.barium.visual_effects").formatted(net.minecraft.util.Formatting.YELLOW)).build());
            
            performance.addEntry(entryBuilder.startBooleanToggle(Text.translatable("option.barium.disable_texture_animations"), BariumConfig.C.DISABLE_TEXTURE_ANIMATIONS)
                    .setDefaultValue(defaults.DISABLE_TEXTURE_ANIMATIONS).setTooltip(Text.translatable("tooltip.barium.disable_texture_animations")).setSaveConsumer(v -> BariumConfig.C.DISABLE_TEXTURE_ANIMATIONS = v).build());
            performance.addEntry(entryBuilder.startBooleanToggle(Text.translatable("option.barium.cache_debug_hud"), BariumConfig.C.CACHE_DEBUG_HUD)
                    .setDefaultValue(defaults.CACHE_DEBUG_HUD).setTooltip(Text.translatable("tooltip.barium.cache_debug_hud")).setSaveConsumer(v -> BariumConfig.C.CACHE_DEBUG_HUD = v).build());
            performance.addEntry(entryBuilder.startBooleanToggle(Text.translatable("option.barium.enable_tooltip_caching"), BariumConfig.C.ENABLE_TOOLTIP_CACHING)
                    .setDefaultValue(defaults.ENABLE_TOOLTIP_CACHING).setTooltip(Text.translatable("tooltip.barium.enable_tooltip_caching")).setSaveConsumer(v -> BariumConfig.C.ENABLE_TOOLTIP_CACHING = v).build());
            performance.addEntry(entryBuilder.startBooleanToggle(Text.translatable("option.barium.enable_entity_tick_culling"), BariumConfig.C.ENABLE_ENTITY_TICK_CULLING)
                    .setDefaultValue(defaults.ENABLE_ENTITY_TICK_CULLING).setTooltip(Text.translatable("tooltip.barium.enable_entity_tick_culling")).setSaveConsumer(v -> BariumConfig.C.ENABLE_ENTITY_TICK_CULLING = v).build());

            return builder.build();
        };
    }
}