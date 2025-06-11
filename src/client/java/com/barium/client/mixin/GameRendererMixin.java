// --- Crie ou edite o arquivo: src/client/java/com/barium/client/mixin/GameRendererMixin.java ---
package com.barium.client.mixin;

import com.barium.config.BariumConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    /**
     * Injeta no final do método onResized.
     * Depois que o Minecraft redimensiona todos os seus framebuffers, nós interceptamos
     * e forçamos o framebuffer do contorno da entidade para metade da resolução,
     * se a opção estiver ativada.
     */
    @Inject(
        method = "onResized(II)V",
        at = @At("RETURN")
    )
    private void barium$forceResizeEntityOutlineFramebuffer(int width, int height, CallbackInfo ci) {
        if (BariumConfig.C.ENABLE_HALF_RESOLUTION_ENTITY_OUTLINES) {
            // Obtém a referência do framebuffer de contorno de forma segura.
            Framebuffer entityOutlinesFramebuffer = MinecraftClient.getInstance().worldRenderer.getEntityOutlinesFramebuffer();
            
            if (entityOutlinesFramebuffer != null) {
                // Força o redimensionamento para metade da largura e altura da janela.
                entityOutlinesFramebuffer.resize(width / 2, height / 2, MinecraftClient.IS_SYSTEM_MAC);
            }
        }
    }
}