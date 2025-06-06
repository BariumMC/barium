package com.barium.client.mixin;

import com.barium.client.optimization.gui.GuiOptimizer;
import com.barium.config.BariumConfig;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
// CORREÇÃO DAS IMPORTAÇÕES:
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormats;
import net.minecraft.text.Text;

import com.mojang.blaze3d.systems.RenderSystem;
import org.joml.Matrix4f;
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

    // Método helper para desenhar o conteúdo do framebuffer.
    private void drawCachedGui(DrawContext context) {
        Framebuffer framebuffer = GuiOptimizer.getFramebuffer();
        if (framebuffer == null) return;

        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderTexture(0, framebuffer.getColorAttachment());
        RenderSystem.enableBlend();
        
        Tessellator tessellator = Tessellator.getInstance();
        Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();

        tessellator.getBuffer().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        tessellator.getBuffer().vertex(matrix, 0, this.height, 0).texture(0, 0).color(255, 255, 255, 255).next();
        tessellator.getBuffer().vertex(matrix, this.width, this.height, 0).texture(1, 0).color(255, 255, 255, 255).next();
        tessellator.getBuffer().vertex(matrix, this.width, 0, 0).texture(1, 1).color(255, 255, 255, 255).next();
        tessellator.getBuffer().vertex(matrix, 0, 0, 0).texture(0, 1).color(255, 255, 255, 255).next();
        tessellator.draw();

        RenderSystem.disableBlend();
    }
}