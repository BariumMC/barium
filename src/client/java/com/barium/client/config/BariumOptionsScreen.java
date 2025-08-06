package com.barium.client.config;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import java.util.ArrayList;
import java.util.List;

public class BariumOptionsScreen extends Screen {
    private final Screen parent;
    private final List<OptionPage> pages = new ArrayList<>();
    private Screen currentPageContent;
    private OptionPage selectedPage;

    public BariumOptionsScreen(Screen parent) {
        super(Text.translatable("title.barium.options"));
        this.parent = parent;

        // Adiciona as páginas (abas) de configuração
        this.pages.add(new OptionPage(Text.translatable("category.barium.general"), BariumConfigScreenFactory::buildGeneralScreen));
        this.pages.add(new OptionPage(Text.translatable("category.barium.quality"), BariumConfigScreenFactory::buildQualityScreen));
        this.pages.add(new OptionPage(Text.translatable("category.barium.performance"), BariumConfigScreenFactory::buildPerformanceScreen));
        this.pages.add(new OptionPage(Text.translatable("category.barium.advanced"), BariumConfigScreenFactory::buildAdvancedScreen));
    }

    @Override
    protected void init() {
        super.init();

        // Se nenhuma página foi selecionada, seleciona a primeira
        if (this.selectedPage == null) {
            this.selectPage(this.pages.get(0));
        }

        // Adiciona os botões das abas na parte superior
        int tabWidth = 80;
        int tabHeight = 20;
        int startX = this.width / 2 - (this.pages.size() * (tabWidth + 5) - 5) / 2;
        int tabY = 32;

        for (OptionPage page : this.pages) {
            ButtonWidget button = ButtonWidget.builder(page.getTitle(), (btn) -> this.selectPage(page))
                    .dimensions(startX, tabY, tabWidth, tabHeight)
                    .build();

            // Desabilita o botão da aba que já está selecionada
            if (this.selectedPage == page) {
                button.active = false;
            }

            this.addDrawableChild(button);
            startX += tabWidth + 5;
        }

        // Botão "Concluído"
        this.addDrawableChild(ButtonWidget.builder(Text.translatable("gui.done"), button -> this.close())
                .dimensions(this.width / 2 - 100, this.height - 27, 200, 20)
                .build());
    }

    private void selectPage(OptionPage page) {
        this.selectedPage = page;
        // Cria o "conteúdo" da página (que é uma tela, mas vamos usá-la como um contêiner de widgets)
        this.currentPageContent = page.createScreen(this);
        this.currentPageContent.init(this.client, this.width, this.height);

        // Limpa os widgets antigos (exceto os botões de aba e "Concluído") e adiciona os novos
        this.clearChildren();
        this.init(); // Reinicia a tela para recriar os botões de aba

        // Adiciona os widgets da página de conteúdo à tela principal
        this.currentPageContent.children().forEach(this::addDrawableChild);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Renderiza o fundo e o título
        this.renderBackground(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 15, 0xFFFFFF);

        // Renderiza os widgets da página de conteúdo (as opções)
        if (this.currentPageContent != null) {
            // A tela de conteúdo não deve desenhar seu próprio fundo
            for (var drawable : this.currentPageContent.getDrawables()) {
                drawable.render(context, mouseX, mouseY, delta);
            }
        }

        // Renderiza os widgets principais (abas, botão "Concluído")
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public void close() {
        this.client.setScreen(this.parent);
    }
}