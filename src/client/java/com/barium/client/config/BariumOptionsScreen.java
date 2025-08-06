package com.barium.client.config;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
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

    // Listas separadas para gerenciar os diferentes tipos de widgets
    private List<ClickableWidget> tabButtons;
    private List<ClickableWidget> pageWidgets;
    private ButtonWidget doneButton;

    public BariumOptionsScreen(Screen parent) {
        super(Text.translatable("title.barium.options"));
        this.parent = parent;

        // Define as páginas (abas) de configuração
        this.pages.add(new OptionPage(Text.translatable("category.barium.general"), BariumConfigScreenFactory::buildGeneralScreen));
        this.pages.add(new OptionPage(Text.translatable("category.barium.quality"), BariumConfigScreenFactory::buildQualityScreen));
        this.pages.add(new OptionPage(Text.translatable("category.barium.performance"), BariumConfigScreenFactory::buildPerformanceScreen));
        this.pages.add(new OptionPage(Text.translatable("category.barium.advanced"), BariumConfigScreenFactory::buildAdvancedScreen));
    }

    @Override
    protected void init() {
        // Se nenhuma página foi selecionada, seleciona a primeira como padrão.
        if (this.selectedPage == null) {
            this.selectedPage = this.pages.get(0);
        }

        // 1. Cria os botões das abas e o botão "Concluído"
        this.tabButtons = new ArrayList<>();
        int tabWidth = 80;
        int tabHeight = 20;
        int startX = this.width / 2 - (this.pages.size() * (tabWidth + 5) - 5) / 2;
        int tabY = 32;

        for (OptionPage page : this.pages) {
            ButtonWidget button = ButtonWidget.builder(page.getTitle(), (btn) -> this.selectPage(page))
                    .dimensions(startX, tabY, tabWidth, tabHeight)
                    .build();

            if (this.selectedPage == page) {
                button.active = false;
            }

            this.tabButtons.add(button);
            startX += tabWidth + 5;
        }

        this.doneButton = ButtonWidget.builder(Text.translatable("gui.done"), (btn) -> this.close())
                .dimensions(this.width / 2 - 100, this.height - 27, 200, 20)
                .build();

        // 2. Carrega os widgets da página de conteúdo
        this.loadPageWidgets();

        // 3. Adiciona TODOS os widgets à tela
        // A ordem importa para a navegação com Tab
        this.pageWidgets.forEach(this::addDrawableChild);
        this.tabButtons.forEach(this::addDrawableChild);
        this.addDrawableChild(this.doneButton);
    }

    private void loadPageWidgets() {
        // Cria uma tela de conteúdo temporária para pegar os widgets
        Screen contentScreen = this.selectedPage.createScreen(this);
        contentScreen.init(this.client, this.width, this.height);

        // Guarda apenas os widgets da página atual.
        // Usamos `ImmutableList` para garantir que a lista é segura.
        this.pageWidgets = ImmutableList.copyOf(contentScreen.children().stream()
                .filter(e -> e instanceof ClickableWidget)
                .map(e -> (ClickableWidget)e)
                .toList());
    }

    private void selectPage(OptionPage page) {
        if (this.selectedPage != page) {
            this.selectedPage = page;
            // Apenas re-inicializa a tela. O método init() fará todo o trabalho de
            // limpar os widgets antigos e adicionar os novos da página selecionada.
            this.init();
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Renderiza o fundo do mundo (ou o fundo de terra se não estiver no mundo).
        this.renderBackground(context, mouseX, mouseY, delta);

        // Desenha um retângulo escuro semi-transparente para melhorar a legibilidade.
        // Removido o gradiente para um visual mais limpo e próximo do Sodium.
        context.fill(0, 56, this.width, this.height - 32, 0x90000000);

        // O super.render() agora desenhará TUDO que foi adicionado com addDrawableChild():
        // as opções da aba atual, os botões das abas e o botão "Concluído".
        // Ele também cuidará de mostrar as tooltips automaticamente.
        super.render(context, mouseX, mouseY, delta);

        // Desenha o título da tela.
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 15, 0xFFFFFF);
    }

    @Override
    public void close() {
        // Salva todas as configurações pendentes de todas as abas.
        BariumConfigScreenFactory.save();
        // Volta para a tela anterior.
        this.client.setScreen(this.parent);
    }
}