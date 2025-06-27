package com.barium.client.mixin.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin que otimiza a renderização de toda a GUI (HUD, inventários, etc.)
 * desativando a checagem de profundidade (Depth Test) desnecessária.
 */
@Mixin(DrawContext.class)
public class DrawContextMixin {

    /**
     * Injeta ANTES de o método `draw()` começar. Este método é o ponto de partida
     * para desenhar quase tudo na tela (texturas, ícones, etc.).
     */
    @Inject(method = "draw", at = @At("HEAD"))
    private void barium$disableDepthTest(CallbackInfo ci) {
        // Desativa a checagem de profundidade. A GPU não vai mais se preocupar
        // se há algo atrás do elemento da GUI.
        RenderSystem.disableDepthTest();
    }

    /**
     * Injeta DEPOIS que o método `draw()` terminar, usando um "tail injector".
     * Isso garante que a checagem de profundidade seja reativada para que o
     * resto do jogo (o mundo 3D) seja renderizado corretamente.
     */
    @Inject(method = "draw", at = @At("TAIL"))
    private void barium$enableDepthTest(CallbackInfo ci) {
        // Reativa a checagem de profundidade.
        RenderSystem.enableDepthTest();
    }
}