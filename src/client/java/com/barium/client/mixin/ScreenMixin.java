package com.barium.client.mixin;

import com.barium.client.optimization.gui.GuiOptimizer;
import com.barium.config.BariumConfig;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack; // Para a assinatura do método render
import net.minecraft.text.Text; // Para a assinatura do construtor, se necessário

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin para a classe Screen, otimizando a renderização da tela.
 * Implementa caching para reduzir redesenhos desnecessários de elementos da GUI.
 */
@Mixin(Screen.class)
public abstract class ScreenMixin {

    // Shadow para obter a referência à própria tela (necessário para o hashCode)
    @Shadow public int width;
    @Shadow public int height;
    @Shadow public Text title; // Para gerar um hash de estado

    // Construtor abstrato, pois a classe Mixin também é abstrata
    // @SuppressWarnings("InvalidInjector") // Pode ser necessário se o Mixin não reconhecer o construtor
    // private ScreenMixin(Text title) { // Depende do construtor de Screen na 1.21.5
    //     // Não fazer nada, apenas para satisfazer o Mixin
    // }

    // Injeta no início do método render da Screen.
    // A assinatura do render da Screen é geralmente:
    // render(Lnet/minecraft/client/util/math/MatrixStack;IIF)V
    @Inject(
        method = "render(Lnet/minecraft/client/util/math/MatrixStack;IIF)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void barium$optimizeScreenRender(MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (!BariumConfig.ENABLE_GUI_OPTIMIZATION) {
            return; // Se a otimização de GUI estiver desativada, permite renderização normal
        }

        // Gera um "hash" do estado da tela. Isso é simplificado.
        // Em um cenário real, você consideraria os estados de todos os widgets filhos,
        // mas para uma otimização de tela geral, a largura, altura, e título (se mudarem)
        // podem ser um bom indicador de que a tela precisa ser redesenhada.
        // Use uma String ou um objeto que represente o estado atual.
        String currentStateHash = String.format("%d_%d_%s", width, height, title.getString());

        // O 'this' refere-se à instância da Screen que está sendo mixin'ada
        if (!GuiOptimizer.shouldUpdateGuiElement((Screen)(Object)this, currentStateHash)) {
            ci.cancel(); // Cancela a renderização se a tela não precisa ser redesenhada
        }
    }
}
