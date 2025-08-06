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
}