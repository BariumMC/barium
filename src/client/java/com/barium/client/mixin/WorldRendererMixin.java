package com.barium.client.mixin;

import com.barium.config.BariumConfig;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.render.WorldRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin {

    // CORREÇÃO: Removemos o @Shadow, pois ele estava causando o aviso.
    // O framebuffer será obtido diretamente quando necessário.

    @Inject(method = "drawEntityOutlinesFramebuffer", at = @At("HEAD"))
    private void barium$beforeDrawEntityOutlines(CallbackInfo ci) {
        if (BariumConfig.ENABLE_HALF_RESOLUTION_ENTITY_OUTLINES) {
            WorldRenderer self = (WorldRenderer)(Object)this;
            Framebuffer entityOutlinesFramebuffer = self.getEntityOutlinesFramebuffer();
            if (entityOutlinesFramebuffer != null) {
                // Ajustamos o viewport para o tamanho menor do framebuffer.
                RenderSystem.viewport(0, 0, entityOutlinesFramebuffer.textureWidth, entityOutlinesFramebuffer.textureHeight);
            }
        }
    }
    
    @Inject(method = "drawEntityOutlinesFramebuffer", at = @At("RETURN"))
    private void barium$afterDrawEntityOutlines(CallbackInfo ci) {
        if (BariumConfig.ENABLE_HALF_RESOLUTION_ENTITY_OUTLINES) {
            // Restauramos o viewport para o tamanho total da janela.
            MinecraftClient client = MinecraftClient.getInstance();
            RenderSystem.viewport(0, 0, client.getFramebuffer().textureWidth, client.getFramebuffer().textureHeight);
        }
    }

    // O mixin para desativar o contorno pode ser removido se a otimização de meia resolução for preferida.
    // Se você quiser manter a opção de desativar completamente, mantenha este método, senão pode removê-lo.
    @Inject(method = "drawEntityOutlinesFramebuffer", at = @At("HEAD"), cancellable = true)
    private void barium$disableEntityOutlines(CallbackInfo ci) {
        // (Este método de uma etapa anterior pode ser removido se não for mais desejado)
        if (BariumConfig.DISABLE_ENTITY_OUTLINES) {
            ci.cancel();
        }
    }
}