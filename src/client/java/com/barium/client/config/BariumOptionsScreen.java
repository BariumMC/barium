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
    private OptionPage selectedPage;

    // A tela de conteúdo do Cloth Config que será renderizada dentro da nossa.
    private Screen currentPageContent;

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

        // 1. Cria e inicializa a tela de conteúdo da aba selecionada
        this.currentPageContent = this.selectedPage.createScreen(this);
        // IMPORTANTE: Inicializamos a tela de conteúdo para que ela crie seus próprios widgets
        this.currentPageContent.init(this.client, this.width, this.height);

        // 2. Adiciona os botões das abas à NOSSA tela
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
            this.addDrawableChild(button);
            startX += tabWidth + 5;
        }

        // 3. Adiciona o botão "Concluído"
        this.addDrawableChild(ButtonWidget.builder(Text.translatable("gui.done"), (btn) -> this.close())
                .dimensions(this.width / 2 - 100, this.height - 27, 200, 20)
                .build());
    }

    private void selectPage(OptionPage page) {
        if (this.selectedPage != page) {
            // Salva as alterações da página atual antes de trocar
            BariumConfigScreenFactory.save();
            this.selectedPage = page;
            this.init(); // Recria toda a interface com a nova página
        }
    }

    // --- A MÁGICA ACONTECE AQUI ---
    // Nós passamos todos os eventos de mouse e teclado para a tela de conteúdo do Cloth Config

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Primeiro, tenta o clique na nossa tela (abas, botão Concluído)
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        // Se não, passa o clique para a tela de conteúdo
        if (this.currentPageContent != null) {
            return this.currentPageContent.mouseClicked(mouseX, mouseY, button);
        }
        return false;
    }
    
    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (this.currentPageContent != null) {
            return this.currentPageContent.mouseReleased(mouseX, mouseY, button);
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }
    
    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (this.currentPageContent != null) {
            return this.currentPageContent.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (this.currentPageContent != null) {
            return this.currentPageContent.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }
    
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.currentPageContent != null && this.currentPageContent.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // A tela de conteúdo do Cloth Config desenha seu próprio fundo e opções
        if (this.currentPageContent != null) {
            this.currentPageContent.render(context, mouseX, mouseY, delta);
        }

        // Agora, desenhamos NOSSOS widgets (abas, título, botão Concluído) por cima
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 15, 0xFFFFFF);
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public void close() {
        BariumConfigScreenFactory.save();
        this.client.setScreen(this.parent);
    }
}