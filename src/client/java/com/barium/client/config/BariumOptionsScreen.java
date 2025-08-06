package com.barium.client.config;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class BariumOptionsScreen extends Screen {
    private final Screen parent;
    private final List<OptionPage> pages = new ArrayList<>();
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
        // Seleciona a primeira página se nenhuma estiver selecionada
        if (this.selectedPage == null) {
            this.selectedPage = this.pages.get(0);
        }

        // Limpa completamente os widgets da tela antes de reconstruir
        this.clearChildren();

        // 1. Adiciona os widgets da página de conteúdo atual
        // Cria a tela de conteúdo temporariamente para pegar seus widgets
        Screen contentScreen = this.selectedPage.createScreen(this);
        contentScreen.init(this.client, this.width, this.height);

        // Adiciona os widgets da tela de conteúdo à lista de renderização e eventos da tela principal.
        // Isso resolve o erro de compilação, pois children() retorna uma lista do tipo correto.
        for (var child : contentScreen.children()) {
            if (child instanceof ClickableWidget) {
                this.addDrawableChild((ClickableWidget) child);
            }
        }

        // 2. Agora, cria e adiciona os botões das abas por cima
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

        // 3. Botão "Concluído"
        this.addDrawableChild(ButtonWidget.builder(Text.translatable("gui.done"), (btn) -> this.close())
                .dimensions(this.width / 2 - 100, this.height - 27, 200, 20)
                .build());
    }

    private void selectPage(OptionPage page) {
        // Se a página já está selecionada, não faz nada
        if (this.selectedPage == page) {
            return;
        }
        this.selectedPage = page;
        // Re-inicializa a tela inteira. O método init() cuidará de limpar
        // os widgets antigos e adicionar os novos da página selecionada.
        this.init();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        // Chama super.render() para desenhar todos os widgets que foram adicionados via addDrawableChild()
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 15, 0xFFFFFF);
    }

    @Override
    public void close() {
        // CORREÇÃO: Chamamos o método de salvamento aqui.
        // Isso garante que todas as alterações feitas em qualquer aba sejam
        // gravadas nos arquivos de configuração quando o usuário sair da tela.
        BariumConfigScreenFactory.save();

        // Agora, podemos fechar a tela e voltar para a anterior.
        this.client.setScreen(this.parent);
    }
}