// --- Substitua o conteúdo em: src/client/java/com/barium/client/mixin/WorldRendererMixin.java ---
package com.barium.client.mixin;

import com.barium.config.BariumConfig;
import com.mojang.blaze3d.systems.RenderSystem;
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

    @Shadow public abstract Framebuffer getEntityOutlinesFramebuffer();

    /**
     * Injeta no início do método que desenha o framebuffer de contorno.
     * Este é o ponto de controle central para ambas as nossas otimizações.
     */
    @Inject(method = "drawEntityOutlinesFramebuffer", at = @At("HEAD"), cancellable = true)
    private void barium$controlEntityOutlines(CallbackInfo ci) {
        // Otimização 1: Desativar completamente (maior prioridade)
        if (BariumConfig.C.DISABLE_ENTITY_OUTLINES) {
            ci.cancel(); // Se desativado, cancela todo o método. Nada mais é executado.
            return;
        }

        // Otimização 2: Renderizar em meia resolução (só executa se a otimização 1 não foi ativada)
        if (BariumConfig.C.ENABLE_HALF_RESOLUTION_ENTITY_OUTLINES) {
            Framebuffer entityOutlinesFramebuffer = this.getEntityOutlinesFramebuffer();
            if (entityOutlinesFramebuffer != null) {
                // CORREÇÃO: O método agora só aceita 4 argumentos.
                RenderSystem.viewport(0, 0, entityOutlinesFramebuffer.textureWidth, entityOutlinesFramebuffer.textureHeight);
            }
        }
    }
    
    /**
     * Injeta no final do método para restaurar o viewport.
     * Este método só será executado se a injeção em "HEAD" não tiver sido cancelada.
     */
    @Inject(method = "drawEntityOutlinesFramebuffer", at = @At("RETURN"))
    private void barium$afterDrawEntityOutlines(CallbackInfo ci) {
        // Se a otimização de meia resolução foi usada, precisamos restaurar o viewport
        // para o tamanho total da janela, para que o resto do jogo (GUI, etc.)
        // seja renderizado corretamente.
        if (BariumConfig.C.ENABLE_HALF_RESOLUTION_ENTITY_OUTLINES) {
            MinecraftClient client = MinecraftClient.getInstance();
            // CORREÇÃO: O método agora só aceita 4 argumentos.
            RenderSystem.viewport(0, 0, client.getFramebuffer().textureWidth, client.getFramebuffer().textureHeight);
        }
    }
}