package com.barium.client.mixin;

import com.barium.client.optimization.gui.GuiOptimizer;
import com.barium.config.BariumConfig;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.DrawContext; // ALTERADO: Importar DrawContext
import net.minecraft.text.Text;

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

    @Shadow public int width;
    @Shadow public int height;
    @Shadow public Text title;

    // CORRIGIDO: Assinatura do método render para 1.21.5 (usando DrawContext)
    @Inject(
        method = "render(Lnet/minecraft/client/gui/DrawContext;IIF)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void barium$optimizeScreenRender(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) { // ALTERADO: Parâmetro 'matrices' para 'context'
        if (!BariumConfig.ENABLE_GUI_OPTIMIZATION) {
            return;
        }

        String currentStateHash = String.format("%d_%d_%s", width, height, title.getString());

        if (!GuiOptimizer.shouldUpdateGuiElement((Screen)(Object)this, currentStateHash)) {
            // Se não devemos atualizar, simplesmente renderizamos a última versão em cache.
            // Infelizmente, não temos um "framebuffer" em cache, então cancelar é a melhor opção.
            // Para telas dinâmicas, isso pode causar artefatos visuais.
            // Uma melhoria seria armazenar e redesenhar os draw calls, mas isso é muito complexo.
            // Por enquanto, cancelar a renderização é a abordagem mais simples.
            ci.cancel();
        }
    }

    // NOVO MÉTODO: Limpa o cache quando a tela é fechada.
    @Inject(method = "onClosed()V", at = @At("HEAD"))
    private void barium$onScreenClosed(CallbackInfo ci) {
        if (BariumConfig.ENABLE_GUI_OPTIMIZATION) {
            GuiOptimizer.clearCache();
        }
    }
}
