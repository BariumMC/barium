package com.barium.client.config;

import com.barium.config.BariumConfig;
import com.barium.config.ConfigData;
import com.barium.config.ConfigManager;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.option.CloudRenderMode;
import net.minecraft.client.option.GraphicsMode;
import net.minecraft.client.option.ParticleVisibility; // <-- ESTE É O IMPORT CORRETO E FINAL.
import net.minecraft.text.Text;

public class BariumVideoSettingsScreen {

    public static Screen build(Screen parent) {
        MinecraftClient client = MinecraftClient.getInstance();
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Text.translatable("title.barium.video_settings"));

        builder.setSavingRunnable(() -> {
            ConfigManager.saveConfig();
            client.options.write();
        });

        ConfigData defaults = new ConfigData();
        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        // ABA: GERAL
        ConfigCategory general = builder.getOrCreateCategory(Text.translatable("category.barium.general"));
        general.addEntry(entryBuilder.startIntSlider(Text.translatable("options.renderDistance"), client.options.getViewDistance().getValue(), 2, 32).setDefaultValue(12).setSaveConsumer(newValue -> client.options.getViewDistance().setValue(newValue)).build());
        general.addEntry(entryBuilder.startIntSlider(Text.translatable("options.gamma"), (int) (client.options.getGamma().getValue() * 100), 0, 100).setDefaultValue(50).setTextGetter(value -> Text.of(value == 0 ? "Mínimo" : (value == 100 ? "Máximo" : value + "%"))).setSaveConsumer(newValue -> client.options.getGamma().setValue(newValue / 100.0)).build());
        general.addEntry(entryBuilder.startBooleanToggle(Text.translatable("options.vsync"), client.options.getEnableVsync().getValue()).setDefaultValue(true).setSaveConsumer(newValue -> client.options.getEnableVsync().setValue(newValue)).build());
        general.addEntry(entryBuilder.startIntSlider(Text.translatable("options.maxFps"), client.options.getMaxFps().getValue(), 10, 260).setDefaultValue(120).setTextGetter(value -> value == 260 ? Text.translatable("options.framerateLimit.max") : Text.of(value + " FPS"))
                .setSaveConsumer(newValue -> client.options.getMaxFps().setValue(newValue)).build());

        // ABA: QUALIDADE
        ConfigCategory quality = builder.getOrCreateCategory(Text.translatable("category.barium.quality"));
        quality.addEntry(entryBuilder.startEnumSelector(Text.translatable("options.graphics"), GraphicsMode.class, client.options.getGraphicsMode().getValue()).setDefaultValue(GraphicsMode.FANCY).setSaveConsumer(newValue -> client.options.getGraphicsMode().setValue(newValue)).build());
        quality.addEntry(entryBuilder.startEnumSelector(Text.translatable("options.clouds"), CloudRenderMode.class, client.options.getCloudRenderMode().getValue()).setDefaultValue(CloudRenderMode.FANCY).setSaveConsumer(newValue -> client.options.getCloudRenderMode().setValue(newValue)).build());
        
        // CORREÇÃO FINAL: Usando a classe ParticleVisibility correta.
        quality.addEntry(entryBuilder.startEnumSelector(Text.translatable("options.particles"), ParticleVisibility.class, client.options.getParticles().getValue())
                .setDefaultValue(ParticleVisibility.ALL)
                .setSaveConsumer(newValue -> client.options.getParticles().setValue(newValue))
                .build());
        
        quality.addEntry(entryBuilder.startIntSlider(Text.translatable("options.mipmapLevels"), client.options.getMipmapLevels().getValue(), 0, 4).setDefaultValue(4).setSaveConsumer(newValue -> client.options.getMipmapLevels().setValue(newValue)).build());

        // ABA: PERFORMANCE
        ConfigCategory performance = builder.getOrCreateCategory(Text.translatable("category.barium.performance"));
        performance.addEntry(entryBuilder.startBooleanToggle(Text.translatable("option.barium.enable_directional_chunk_loading"), BariumConfig.C.ENABLE_DIRECTIONAL_CHUNK_LOADING).setDefaultValue(defaults.ENABLE_DIRECTIONAL_CHUNK_LOADING).setTooltip(Text.translatable("tooltip.barium.enable_directional_chunk_loading")).setSaveConsumer(newValue -> BariumConfig.C.ENABLE_DIRECTIONAL_CHUNK_LOADING = newValue).build());
        performance.addEntry(entryBuilder.startBooleanToggle(Text.translatable("option.barium.enable_chunk_update_throttling"), BariumConfig.C.ENABLE_CHUNK_UPDATE_THROTTLING).setDefaultValue(defaults.ENABLE_CHUNK_UPDATE_THROTTLING).setTooltip(Text.translatable("tooltip.barium.enable_chunk_update_throttling")).setSaveConsumer(newValue -> BariumConfig.C.ENABLE_CHUNK_UPDATE_THROTTLING = newValue).build());
        performance.addEntry(entryBuilder.startIntSlider(Text.translatable("option.barium.max_chunk_uploads"), BariumConfig.C.MAX_CHUNK_UPLOADS_PER_FRAME, 1, 16).setDefaultValue(defaults.MAX_CHUNK_UPLOADS_PER_FRAME).setTooltip(Text.translatable("tooltip.barium.max_chunk_uploads")).setSaveConsumer(newValue -> BariumConfig.C.MAX_CHUNK_UPLOADS_PER_FRAME = newValue).build());

        // ABA: AVANÇADO
        ConfigCategory advanced = builder.getOrCreateCategory(Text.translatable("category.barium.advanced"));
        advanced.addEntry(entryBuilder.startBooleanToggle(Text.translatable("option.barium.enable_entity_culling"), BariumConfig.C.ENABLE_ENTITY_CULLING).setDefaultValue(defaults.ENABLE_ENTITY_CULLING).setTooltip(Text.translatable("tooltip.barium.enable_entity_culling")).setSaveConsumer(newValue -> BariumConfig.C.ENABLE_ENTITY_CULLING = newValue).build());
        advanced.addEntry(entryBuilder.startBooleanToggle(Text.translatable("option.barium.disable_texture_animations"), BariumConfig.C.DISABLE_TEXTURE_ANIMATIONS).setDefaultValue(defaults.DISABLE_TEXTURE_ANIMATIONS).setTooltip(Text.translatable("tooltip.barium.disable_texture_animations")).setSaveConsumer(newValue -> BariumConfig.C.DISABLE_TEXTURE_ANIMATIONS = newValue).build());
                
        return builder.build();
    }
}