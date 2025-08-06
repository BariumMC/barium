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

        int buttonWidth = 150;
        int buttonHeight = 20;
        int spacing = 5;
        int startY = this.height / 2 - (4 * (buttonHeight + spacing)) / 2; // Centraliza 4 botões verticalmente
        int centerX = this.width / 2 - buttonWidth / 2;

        // Botão "Geral"
        this.addDrawableChild(ButtonWidget.builder(Text.translatable("category.barium.general"), button -> {
            this.client.setScreen(BariumConfigScreenFactory.buildGeneralScreen(this));
        }).dimensions(centerX, startY, buttonWidth, buttonHeight).build());

        // Botão "Qualidade"
        startY += buttonHeight + spacing;
        this.addDrawableChild(ButtonWidget.builder(Text.translatable("category.barium.quality"), button -> {
            this.client.setScreen(BariumConfigScreenFactory.buildQualityScreen(this));
        }).dimensions(centerX, startY, buttonWidth, buttonHeight).build());

        // Botão "Desempenho"
        startY += buttonHeight + spacing;
        this.addDrawableChild(ButtonWidget.builder(Text.translatable("category.barium.performance"), button -> {
            this.client.setScreen(BariumConfigScreenFactory.buildPerformanceScreen(this));
        }).dimensions(centerX, startY, buttonWidth, buttonHeight).build());

        // Botão "Avançado"
        startY += buttonHeight + spacing;
        this.addDrawableChild(ButtonWidget.builder(Text.translatable("category.barium.advanced"), button -> {
            this.client.setScreen(BariumConfigScreenFactory.buildAdvancedScreen(this));
        }).dimensions(centerX, startY, buttonWidth, buttonHeight).build());

        // Botão "Concluído" na parte inferior
        this.addDrawableChild(ButtonWidget.builder(Text.translatable("gui.done"), button -> {
            this.client.setScreen(this.parent);
        }).dimensions(this.width / 2 - 100, this.height - 27, 200, 20).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Renderiza o fundo do mundo (ou o fundo padrão)
        this.renderBackground(context, mouseX, mouseY, delta);
        
        // Desenha todos os botões que foram adicionados com addDrawableChild()
        super.render(context, mouseX, mouseY, delta);
        
        // Desenha o título da tela
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 15, 0xFFFFFF);
    }

    @Override
    public void close() {
        // Ao fechar, volta para a tela de opções principal do Minecraft.
        this.client.setScreen(this.parent);
    }
}