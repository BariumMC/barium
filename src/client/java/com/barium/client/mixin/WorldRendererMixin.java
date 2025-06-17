// --- Substitua o conteúdo em: src/client/java/com/barium/client/mixin/WorldRendererMixin.java ---
package com.barium.client.mixin;

import com.barium.config.BariumConfig;
import net.minecraft.client.render.WorldRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin {

    /**
     * CORREÇÃO FINAL: A otimização de meia resolução foi removida pois o método para
     * controlar o viewport (`RenderSystem.viewport`) foi removido do Minecraft.
     * Esta versão mantém apenas a otimização funcional de desativar completamente o efeito.
     */
    @Inject(method = "drawEntityOutlinesFramebuffer", at = @At("HEAD"), cancellable = true)
    private void barium$controlEntityOutlines(CallbackInfo ci) {
        // Se a opção para desativar os contornos estiver ligada, cancela o método.
        if (BariumConfig.C.DISABLE_ENTITY_OUTLINES) {
            ci.cancel();
        }
    }
}