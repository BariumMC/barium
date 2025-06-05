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
            ci.cancel();
        }
    }
}
