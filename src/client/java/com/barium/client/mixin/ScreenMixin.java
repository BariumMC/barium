package com.barium.client.mixin;

import com.barium.client.optimization.gui.GuiOptimizer;
import com.barium.config.BariumConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public abstract class ScreenMixin {

    @Shadow public int width;
    @Shadow public int height;
    @Shadow public Text title;

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void barium$cacheOrRenderScreen(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (!BariumConfig.ENABLE_GUI_OPTIMIZATION) {
            return;
        }

        Screen self = (Screen) (Object) this;
        // Criamos um hash com mais informações para detectar mudanças no mouse.
        String currentStateHash = String.format("%d_%d_%s_%d_%d", width, height, title.getString(), mouseX, mouseY);

        if (GuiOptimizer.shouldUseCache(self, currentStateHash)) {
            // Se o cache é válido, desenhamos a imagem salva e cancelamos o resto.
            GuiOptimizer.drawCachedGui();
            ci.cancel();
        } else {
            // Se o cache é inválido, começamos a gravar no nosso framebuffer.
            GuiOptimizer.beginCacheRender();
        }
    }

    // Injeta no final da renderização para finalizar a gravação do cache.
    @Inject(method = "render", at = @At("RETURN"))
    private void barium$endCacheRender(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (BariumConfig.ENABLE_GUI_OPTIMIZATION) {
            GuiOptimizer.endCacheRender();
            // Agora que a renderização foi para o cache, desenhamos o cache na tela.
            GuiOptimizer.drawCachedGui();
        }
    }

    // Injeta quando a tela é fechada/removida para limpar os recursos.
    @Inject(method = "removed", at = @At("HEAD"))
    private void barium$onScreenRemoved(CallbackInfo ci) {
        if (BariumConfig.ENABLE_GUI_OPTIMIZATION) {
            GuiOptimizer.invalidateCache();
        }
    }
}