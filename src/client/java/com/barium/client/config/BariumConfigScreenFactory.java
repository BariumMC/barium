package com.barium.client.config;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

public class BariumOptionsScreen extends Screen {
    private final Screen parent;

    public BariumOptionsScreen(Screen parent) {
        super(Text.translatable("title.barium.options"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();

        int tabWidth = 80;
        int tabHeight = 20;
        int startX = this.width / 2 - (4 * tabWidth + 3 * 5) / 2; // Centraliza 4 abas com 5px de espaço
        int tabY = 32;

        // Botão "Aba" Geral
        this.addDrawableChild(ButtonWidget.builder(Text.translatable("category.barium.general"), button -> {
            this.client.setScreen(BariumConfigScreenFactory.buildGeneralScreen(this));
        }).dimensions(startX, tabY, tabWidth, tabHeight).build());

        // Botão "Aba" Qualidade
        this.addDrawableChild(ButtonWidget.builder(Text.translatable("category.barium.quality"), button -> {
            this.client.setScreen(BariumConfigScreenFactory.buildQualityScreen(this));
        }).dimensions(startX + tabWidth + 5, tabY, tabWidth, tabHeight).build());

        // Botão "Aba" Performance
        this.addDrawableChild(ButtonWidget.builder(Text.translatable("category.barium.performance"), button -> {
            this.client.setScreen(BariumConfigScreenFactory.buildPerformanceScreen(this));
        }).dimensions(startX + 2 * (tabWidth + 5), tabY, tabWidth, tabHeight).build());
        
        // Botão "Aba" Avançado
        this.addDrawableChild(ButtonWidget.builder(Text.translatable("category.barium.advanced"), button -> {
            this.client.setScreen(BariumConfigScreenFactory.buildAdvancedScreen(this));
        }).dimensions(startX + 3 * (tabWidth + 5), tabY, tabWidth, tabHeight).build());

        // Botão "Concluído"
        this.addDrawableChild(ButtonWidget.builder(Text.translatable("gui.done"), button -> {
            this.client.setScreen(this.parent);
        }).dimensions(this.width / 2 - 100, this.height - 27, 200, 20).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 15, 0xFFFFFF);
    }

    @Override
    public void close() {
        this.client.setScreen(this.parent);
    }
}```

#### 2. A Fábrica de Telas de Configuração (As "Abas")

Este helper irá construir cada tela de configuração individualmente.

**Ação:** Crie o seguinte novo arquivo:
**`barium-1.21.8/src/client/java/com/barium/client/config/BariumConfigScreenFactory.java`**
```java
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
import net.minecraft.text.Text;

public class BariumConfigScreenFactory {

    private static void save() {
        ConfigManager.saveConfig();
        MinecraftClient.getInstance().options.write();
    }

    public static Screen buildGeneralScreen(Screen parent) {
        MinecraftClient client = MinecraftClient.getInstance();
        ConfigBuilder builder = ConfigBuilder.create().setParentScreen(parent).setTitle(Text.translatable("category.barium.general")).setSavingRunnable(BariumConfigScreenFactory::save);
        ConfigEntryBuilder entryBuilder = builder.entryBuilder();
        ConfigCategory general = builder.getOrCreateCategory(Text.translatable("category.barium.general"));

        general.addEntry(entryBuilder.startIntSlider(Text.translatable("options.renderDistance"), client.options.getViewDistance().getValue(), 2, 32).setDefaultValue(12).setSaveConsumer(newValue -> client.options.getViewDistance().setValue(newValue)).build());
        general.addEntry(entryBuilder.startIntSlider(Text.translatable("options.gamma"), (int) (client.options.getGamma().getValue() * 100), 0, 100).setDefaultValue(50).setTextGetter(value -> Text.of(value == 0 ? "Mínimo" : (value == 100 ? "Máximo" : value + "%"))).setSaveConsumer(newValue -> client.options.getGamma().setValue(newValue / 100.0)).build());
        general.addEntry(entryBuilder.startBooleanToggle(Text.translatable("options.vsync"), client.options.getEnableVsync().getValue()).setDefaultValue(true).setSaveConsumer(newValue -> client.options.getEnableVsync().setValue(newValue)).build());
        general.addEntry(entryBuilder.startIntSlider(Text.translatable("options.maxFps"), client.options.getMaxFps().getValue(), 10, 260).setDefaultValue(120).setTextGetter(value -> value == 260 ? Text.translatable("options.framerateLimit.max") : Text.of(value + " FPS")).setSaveConsumer(newValue -> client.options.getMaxFps().setValue(newValue)).build());
        
        return builder.build();
    }
    
    public static Screen buildQualityScreen(Screen parent) {
        MinecraftClient client = MinecraftClient.getInstance();
        ConfigBuilder builder = ConfigBuilder.create().setParentScreen(parent).setTitle(Text.translatable("category.barium.quality")).setSavingRunnable(BariumConfigScreenFactory::save);
        ConfigEntryBuilder entryBuilder = builder.entryBuilder();
        ConfigCategory quality = builder.getOrCreateCategory(Text.translatable("category.barium.quality"));

        quality.addEntry(entryBuilder.startEnumSelector(Text.translatable("options.graphics"), GraphicsMode.class, client.options.getGraphicsMode().getValue()).setDefaultValue(GraphicsMode.FANCY).setSaveConsumer(newValue -> client.options.getGraphicsMode().setValue(newValue)).build());
        quality.addEntry(entryBuilder.startEnumSelector(Text.translatable("options.clouds"), CloudRenderMode.class, client.options.getCloudRenderMode().getValue()).setDefaultValue(CloudRenderMode.FANCY).setSaveConsumer(newValue -> client.options.getCloudRenderMode().setValue(newValue)).build());
        quality.addEntry(entryBuilder.startIntSlider(Text.translatable("options.mipmapLevels"), client.options.getMipmapLevels().getValue(), 0, 4).setDefaultValue(4).setSaveConsumer(newValue -> client.options.getMipmapLevels().setValue(newValue)).build());
        
        return builder.build();
    }
    
    public static Screen buildPerformanceScreen(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create().setParentScreen(parent).setTitle(Text.translatable("category.barium.performance")).setSavingRunnable(BariumConfigScreenFactory::save);
        ConfigEntryBuilder entryBuilder = builder.entryBuilder();
        ConfigData defaults = new ConfigData();
        ConfigCategory performance = builder.getOrCreateCategory(Text.translatable("category.barium.performance"));

        performance.addEntry(entryBuilder.startBooleanToggle(Text.translatable("option.barium.enable_directional_chunk_loading"), BariumConfig.C.ENABLE_DIRECTIONAL_CHUNK_LOADING).setDefaultValue(defaults.ENABLE_DIRECTIONAL_CHUNK_LOADING).setTooltip(Text.translatable("tooltip.barium.enable_directional_chunk_loading")).setSaveConsumer(newValue -> BariumConfig.C.ENABLE_DIRECTIONAL_CHUNK_LOADING = newValue).build());
        performance.addEntry(entryBuilder.startBooleanToggle(Text.translatable("option.barium.enable_chunk_update_throttling"), BariumConfig.C.ENABLE_CHUNK_UPDATE_THROTTLING).setDefaultValue(defaults.ENABLE_CHUNK_UPDATE_THROTTLING).setTooltip(Text.translatable("tooltip.barium.enable_chunk_update_throttling")).setSaveConsumer(newValue -> BariumConfig.C.ENABLE_CHUNK_UPDATE_THROTTLING = newValue).build());
        performance.addEntry(entryBuilder.startIntSlider(Text.translatable("option.barium.max_chunk_uploads"), BariumConfig.C.MAX_CHUNK_UPLOADS_PER_FRAME, 1, 16).setDefaultValue(defaults.MAX_CHUNK_UPLOADS_PER_FRAME).setTooltip(Text.translatable("tooltip.barium.max_chunk_uploads")).setSaveConsumer(newValue -> BariumConfig.C.MAX_CHUNK_UPLOADS_PER_FRAME = newValue).build());

        return builder.build();
    }
    
    public static Screen buildAdvancedScreen(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create().setParentScreen(parent).setTitle(Text.translatable("category.barium.advanced")).setSavingRunnable(BariumConfigScreenFactory::save);
        ConfigEntryBuilder entryBuilder = builder.entryBuilder();
        ConfigData defaults = new ConfigData();
        ConfigCategory advanced = builder.getOrCreateCategory(Text.translatable("category.barium.advanced"));

        advanced.addEntry(entryBuilder.startBooleanToggle(Text.translatable("option.barium.enable_entity_culling"), BariumConfig.C.ENABLE_ENTITY_CULLING).setDefaultValue(defaults.ENABLE_ENTITY_CULLING).setTooltip(Text.translatable("tooltip.barium.enable_entity_culling")).setSaveConsumer(newValue -> BariumConfig.C.ENABLE_ENTITY_CULLING = newValue).build());
        advanced.addEntry(entryBuilder.startBooleanToggle(Text.translatable("option.barium.disable_texture_animations"), BariumConfig.C.DISABLE_TEXTURE_ANIMATIONS).setDefaultValue(defaults.DISABLE_TEXTURE_ANIMATIONS).setTooltip(Text.translatable("tooltip.barium.disable_texture_animations")).setSaveConsumer(newValue -> BariumConfig.C.DISABLE_TEXTURE_ANIMATIONS = newValue).build());

        return builder.build();
    }
}