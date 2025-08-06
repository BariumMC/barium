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

        // Define as páginas (abas) de configuração
        this.pages.add(new OptionPage(Text.translatable("category.barium.general"), BariumConfigScreenFactory::buildGeneralScreen));
        this.pages.add(new OptionPage(Text.translatable("category.barium.quality"), BariumConfigScreenFactory::buildQualityScreen));
        this.pages.add(new OptionPage(Text.translatable("category.barium.performance"), BariumConfigScreenFactory::buildPerformanceScreen));
        this.pages.add(new OptionPage(Text.translatable("category.barium.advanced"), BariumConfigScreenFactory::buildAdvancedScreen));
    }

    @Override
    protected void init() {
        // Se nenhuma página foi selecionada ainda, seleciona a primeira como padrão.
        if (this.selectedPage == null) {
            this.selectedPage = this.pages.get(0);
        }

        // Limpa TODOS os widgets da tela antes de reconstruir a interface.
        this.clearChildren();

        // 1. ADICIONA OS WIDGETS DAS OPÇÕES (SLIDERS, BOTÕES, ETC.)
        // Cria uma tela de conteúdo temporária APENAS para pegar os widgets que o Cloth Config gera.
        Screen contentScreen = this.selectedPage.createScreen(this);
        contentScreen.init(this.client, this.width, this.height);

        // Adiciona cada widget da tela de conteúdo à nossa tela principal.
        // Agora eles serão renderizados e interativos.
        for (var child : contentScreen.children()) {
            if (child instanceof ClickableWidget) {
                this.addDrawableChild((ClickableWidget) child);
            }
        }

        // 2. ADICIONA OS BOTÕES DAS ABAS
        int tabWidth = 80;
        int tabHeight = 20;
        int startX = this.width / 2 - (this.pages.size() * (tabWidth + 5) - 5) / 2; // Centraliza as abas
        int tabY = 32;

        for (OptionPage page : this.pages) {
            ButtonWidget button = ButtonWidget.builder(page.getTitle(), (btn) -> this.selectPage(page))
                    .dimensions(startX, tabY, tabWidth, tabHeight)
                    .build();

            // Desabilita o botão da aba que já está selecionada para feedback visual.
            if (this.selectedPage == page) {
                button.active = false;
            }

            this.addDrawableChild(button);
            startX += tabWidth + 5;
        }

        // 3. ADICIONA O BOTÃO "CONCLUÍDO"
        this.addDrawableChild(ButtonWidget.builder(Text.translatable("gui.done"), (btn) -> this.close())
                .dimensions(this.width / 2 - 100, this.height - 27, 200, 20)
                .build());
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

        // APRIMORAMENTO VISUAL: Desenha um retângulo escuro semi-transparente
        // sob as opções para melhorar a legibilidade.
        context.fillGradient(0, 56, this.width, this.height - 32, 0x00000000, 0xC0000000);

        // O super.render() agora desenhará TUDO que foi adicionado com addDrawableChild():
        // as opções da aba atual, os botões das abas e o botão "Concluído".
        // Ele também cuidará de mostrar as tooltips automaticamente!
        super.render(context, mouseX, mouseY, delta);

        // APRIMORAMENTO VISUAL: Desenha o título da tela.
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