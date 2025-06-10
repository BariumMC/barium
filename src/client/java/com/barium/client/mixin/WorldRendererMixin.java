package com.barium.client.mixin;

import com.barium.config.BariumConfig;
import com.mojang.blaze3d.systems.RenderSystem; // Adicione esta importação
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.render.WorldRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin {

    // (Outros mixins nesta classe podem permanecer)
    @Shadow private Framebuffer entityOutlinesFramebuffer;

    // Injetamos ANTES de o framebuffer de contorno ser desenhado.
    @Inject(method = "drawEntityOutlinesFramebuffer", at = @At("HEAD"))
    private void barium$beforeDrawEntityOutlines(CallbackInfo ci) {
        if (BariumConfig.ENABLE_HALF_RESOLUTION_ENTITY_OUTLINES) {
            // Se a otimização estiver ativa, ajustamos o viewport para o tamanho menor do framebuffer.
            RenderSystem.viewport(0, 0, this.entityOutlinesFramebuffer.textureWidth, this.entityOutlinesFramebuffer.textureHeight);
        }
    }
    
    // Injetamos DEPOIS que o framebuffer de contorno foi desenhado para restaurar o viewport.
    @Inject(method = "drawEntityOutlinesFramebuffer", at = @At("RETURN"))
    private void barium$afterDrawEntityOutlines(CallbackInfo ci) {
        if (BariumConfig.ENABLE_HALF_RESOLUTION_ENTITY_OUTLINES) {
            // Restauramos o viewport para o tamanho total da janela para que o resto da HUD renderize corretamente.
            MinecraftClient client = MinecraftClient.getInstance();
            RenderSystem.viewport(0, 0, client.getFramebuffer().textureWidth, client.getFramebuffer().textureHeight);
        }
    }
}