package com.barium.client.config;

import com.barium.config.BariumConfig;
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
                    .setTitle(Text.literal("Barium Config"));

            // Em um sistema de salvamento real, você chamaria config.save() aqui.
            // Por enquanto, as mudanças só afetam a sessão atual do jogo.
            builder.setSavingRunnable(() -> {
                // Lógica de salvamento viria aqui.
            });

            ConfigEntryBuilder entryBuilder = builder.entryBuilder();

            // --- Categoria: Renderização ---
            ConfigCategory rendering = builder.getOrCreateCategory(Text.literal("Rendering"));

            rendering.addEntry(entryBuilder.startBooleanToggle(Text.literal("Enable Particle Optimizations"), BariumConfig.ENABLE_PARTICLE_OPTIMIZATION)
                    .setDefaultValue(true)
                    .setSaveConsumer(newValue -> BariumConfig.ENABLE_PARTICLE_OPTIMIZATION = newValue)
                    .build());
            
            rendering.addEntry(entryBuilder.startIntSlider(Text.literal("Max Global Particles"), BariumConfig.MAX_GLOBAL_PARTICLES, 0, 10000)
                    .setDefaultValue(2000)
                    .setSaveConsumer(newValue -> BariumConfig.MAX_GLOBAL_PARTICLES = newValue)
                    .build());

            rendering.addEntry(entryBuilder.startBooleanToggle(Text.literal("Enable Block Entity Culling"), BariumConfig.ENABLE_BLOCK_ENTITY_CULLING)
                    .setDefaultValue(true)
                    .setSaveConsumer(newValue -> BariumConfig.ENABLE_BLOCK_ENTITY_CULLING = newValue)
                    .build());
            
            rendering.addEntry(entryBuilder.startIntSlider(Text.literal("Block Entity Render Distance"), (int) Math.sqrt(BariumConfig.MAX_BLOCK_ENTITY_RENDER_DISTANCE_SQ), 16, 256)
                    .setDefaultValue(64)
                    .setSaveConsumer(newValue -> BariumConfig.MAX_BLOCK_ENTITY_RENDER_DISTANCE_SQ = newValue * newValue)
                    .build());
            
            rendering.addEntry(entryBuilder.startBooleanToggle(Text.literal("Enable Dense Foliage Culling"), BariumConfig.ENABLE_DENSE_FOLIAGE_CULLING)
                    .setDefaultValue(true)
                    .setSaveConsumer(newValue -> BariumConfig.ENABLE_DENSE_FOLIAGE_CULLING = newValue)
                    .build());

            rendering.addEntry(entryBuilder.startIntSlider(Text.literal("Dense Foliage Culling Level (0-3)"), BariumConfig.DENSE_FOLIAGE_CULLING_LEVEL, 0, 3)
                    .setDefaultValue(2)
                    .setSaveConsumer(newValue -> BariumConfig.DENSE_FOLIAGE_CULLING_LEVEL = newValue)
                    .build());

            // --- Categoria: HUD ---
            ConfigCategory hud = builder.getOrCreateCategory(Text.literal("HUD"));

            hud.addEntry(entryBuilder.startBooleanToggle(Text.literal("Enable HUD Optimizations"), BariumConfig.ENABLE_HUD_OPTIMIZATION)
                    .setDefaultValue(true)
                    .setSaveConsumer(newValue -> BariumConfig.ENABLE_HUD_OPTIMIZATION = newValue)
                    .build());

            hud.addEntry(entryBuilder.startBooleanToggle(Text.literal("Disable Toasts (Achievements, etc.)"), BariumConfig.DISABLE_TOASTS)
                    .setDefaultValue(true)
                    .setSaveConsumer(newValue -> BariumConfig.DISABLE_TOASTS = newValue)
                    .build());

            // --- Categoria: Post-Processing ---
            ConfigCategory postProcessing = builder.getOrCreateCategory(Text.literal("Post-Processing"));

            postProcessing.addEntry(entryBuilder.startBooleanToggle(Text.literal("Enable Half-Resolution Entity Outlines"), BariumConfig.ENABLE_HALF_RESOLUTION_ENTITY_OUTLINES)
                    .setDefaultValue(true)
                    .setSaveConsumer(newValue -> BariumConfig.ENABLE_HALF_RESOLUTION_ENTITY_OUTLINES = newValue)
                    .build());
            
            postProcessing.addEntry(entryBuilder.startBooleanToggle(Text.literal("Disable Vignette Effect"), BariumConfig.DISABLE_VIGNETTE)
                    .setDefaultValue(true)
                    .setSaveConsumer(newValue -> BariumConfig.DISABLE_VIGNETTE = newValue)
                    .build());


            return builder.build();
        };
    }
}