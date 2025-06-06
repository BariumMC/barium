package com.barium.client.mixin;

import com.barium.client.optimization.gui.GuiOptimizer;
import com.barium.config.BariumConfig;
import net.minecraft.client.gl.Framebuffer;
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
        if (!BariumConfig.ENABLE_GUI_OPTIMIZATION) return;

        Screen self = (Screen) (Object) this;
        String currentStateHash = String.format("%d_%d_%s_%d_%d", width, height, title.getString(), mouseX, mouseY);

        if (GuiOptimizer.shouldUseCache(self, currentStateHash)) {
            drawCachedGui(context);
            ci.cancel();
        } else {
            GuiOptimizer.beginCacheRender();
        }
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void barium$endCacheRender(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (BariumConfig.ENABLE_GUI_OPTIMIZATION) {
            GuiOptimizer.endCacheRender();
            drawCachedGui(context);
        }
    }

    @Inject(method = "removed", at = @At("HEAD"))
    private void barium$onScreenRemoved(CallbackInfo ci) {
        if (BariumConfig.ENABLE_GUI_OPTIMIZATION) {
            GuiOptimizer.invalidateCache();
        }
    }

    /**
     * Usa o método 'blit' do DrawContext para desenhar o conteúdo do framebuffer.
     * Esta é a forma limpa e moderna, que não precisa de Tessellator ou VertexFormats.
     */
    private void drawCachedGui(DrawContext context) {
        Framebuffer framebuffer = GuiOptimizer.getFramebuffer();
        if (framebuffer == null) return;

        int textureId = framebuffer.getColorAttachment();
        context.blit(textureId, 0, 0, 0, (float)this.height, (float)this.width, (float)-this.height);
    }
}