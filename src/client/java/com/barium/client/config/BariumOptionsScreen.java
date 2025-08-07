package com.barium.client.config;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
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

        int buttonWidth = 150 * 2 + 10; // Largura para dois botões lado a lado
        int smallButtonWidth = 150;
        int buttonHeight = 20;
        int spacing = 5;

        // Centraliza o bloco de botões na tela
        int startY = this.height / 2 - (2 * (buttonHeight + spacing));
        int centerX = this.width / 2;

        // Linha 1: Geral e Qualidade
        this.addDrawableChild(ButtonWidget.builder(Text.translatable("category.barium.general"), button -> {
            this.client.setScreen(BariumConfigScreenFactory.buildGeneralScreen(this));
        }).dimensions(centerX - smallButtonWidth - (spacing / 2), startY, smallButtonWidth, buttonHeight).build());

        this.addDrawableChild(ButtonWidget.builder(Text.translatable("category.barium.quality"), button -> {
            this.client.setScreen(BariumConfigScreenFactory.buildQualityScreen(this));
        }).dimensions(centerX + (spacing / 2), startY, smallButtonWidth, buttonHeight).build());

        // Linha 2: Desempenho e Avançado
        startY += buttonHeight + spacing;
        this.addDrawableChild(ButtonWidget.builder(Text.translatable("category.barium.performance"), button -> {
            this.client.setScreen(BariumConfigScreenFactory.buildPerformanceScreen(this));
        }).dimensions(centerX - smallButtonWidth - (spacing / 2), startY, smallButtonWidth, buttonHeight).build());
        
        this.addDrawableChild(ButtonWidget.builder(Text.translatable("category.barium.advanced"), button -> {
            this.client.setScreen(BariumConfigScreenFactory.buildAdvancedScreen(this));
        }).dimensions(centerX + (spacing / 2), startY, smallButtonWidth, buttonHeight).build());

        // Botão "Concluído" na parte inferior
        this.addDrawableChild(ButtonWidget.builder(Text.translatable("gui.done"), button -> {
            this.client.setScreen(this.parent);
        }).dimensions(this.width / 2 - 100, this.height - 27, 200, 20).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 20, 0xFFFFFF);
    }

    @Override
    public void close() {
        this.client.setScreen(this.parent);
    }
}