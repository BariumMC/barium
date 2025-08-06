package com.barium.client.mixin.gui;

import com.barium.client.config.BariumOptionsScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(OptionsScreen.class)
public abstract class OptionsScreenMixin extends Screen {

    // Construtor necessário para a herança
    protected OptionsScreenMixin(net.minecraft.text.Text title) {
        super(title);
    }

    /**
     * @author Barium
     * @reason Modifica a ação (onPress) do botão de Opções de Vídeo para abrir a tela do Barium.
     * Esta é a abordagem mais segura, pois modifica apenas o comportamento do botão, sem
     * alterar a estrutura da tela ou depender de nomes de construtores.
     */
    @ModifyArg(
        method = "init",
        at = @At(
            value = "INVOKE",
            // O alvo é a chamada ao método 'createButton', que é usado para criar
            // vários botões na tela de opções, incluindo o de vídeo.
            target = "Lnet/minecraft/client/gui/screen/option/OptionsScreen;createButton(Lnet/minecraft/text/Text;Ljava/util/function/Supplier;)Lnet/minecraft/client/gui/widget/ButtonWidget;"
        ),
        // O índice 1 refere-se ao segundo argumento de 'createButton', que é o 'screenSupplier' (a ação do botão).
        index = 1
    )
    private java.util.function.Supplier<Screen> modifyVideoOptionsButtonAction(java.util.function.Supplier<Screen> originalAction) {
        // A tela que seria aberta originalmente é a VideoOptionsScreen.
        // Nós não temos uma maneira 100% segura de verificar qual botão é este,
        // mas na prática, dentro do método init() da OptionsScreen, a única ação
        // que cria uma VideoOptionsScreen é a do botão que queremos.
        //
        // Para sermos mais seguros, poderíamos inspecionar a lambda, mas isso é complexo.
        // A abordagem mais simples é criar uma tela anônima para verificar o tipo.
        Screen s = originalAction.get();
        if (s instanceof net.minecraft.client.gui.screen.option.VideoOptionsScreen) {
            // Se a ação original era abrir a tela de vídeo, nós a substituímos
            // pela nossa ação, que abre a tela do Barium.
            return () -> new BariumOptionsScreen(this);
        }

        // Para todos os outros botões, mantemos a ação original.
        return originalAction;
    }
}