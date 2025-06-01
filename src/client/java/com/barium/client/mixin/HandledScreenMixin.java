package com.barium.client.mixin;

import com.barium.client.optimization.InventoryOptimizer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin para HandledScreen para otimizar a renderização de slots de inventário.
 * Intercepta a chamada de renderização de cada slot para decidir se ela é necessária
 * com base no cache do InventoryOptimizer. Também limpa o cache ao fechar a tela.
 * Baseado nos mappings Yarn 1.21.5+build.1.
 */
@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin extends Screen {

    @Shadow protected ScreenHandler handler;

    // O construtor Screen requer um Text, pode ser null para mixins
    protected HandledScreenMixin() {
        super(null);
    }

    /**
     * Redireciona a chamada para Slot.render() dentro do método render principal de HandledScreen.
     * Isso nos permite decidir se um slot individual deve ser renderizado ou se a chamada original
     * deve ser pulada.
     *
     * Target Method: net.minecraft.client.gui.screen.ingame.HandledScreen.render(Lnet/minecraft/client/gui/DrawContext;IIF)V
     * Target INVOKE: Lnet/minecraft/screen/slot/Slot;render(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/client/gui/screen/ingame/HandledScreen;)V
     */
    @Redirect(
        method = "render(Lnet/minecraft/client/gui/DrawContext;IIF)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/screen/slot/Slot;render(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/client/gui/screen/ingame/HandledScreen;)V")
    )
    private void barium$redirectSlotRender(Slot slot, DrawContext context, HandledScreen<?> screen) {
        // Verifica se o slot precisa ser redesenhado usando o otimizador.
        if (InventoryOptimizer.shouldRedrawSlot(slot)) {
            // Se sim, chama o método original de renderização do slot.
            slot.render(context, screen);
        }
        // Se shouldRedrawSlot retornar false, o método original de renderização do slot não é chamado,
        // otimizando a renderização ao pular itens que não mudaram.
    }

    /**
     * Injeta no final do método close() de HandledScreen para limpar o cache de inventário.
     * Isso é crucial para evitar vazamentos de memória e garantir que o cache seja atualizado
     * corretamente quando uma tela de inventário é fechada e reaberta.
     *
     * Target Method: net.minecraft.client.gui.screen.ingame.HandledScreen.close()V
     */
    @Inject(
        method = "close()V",
        at = @At("TAIL")
    )
    private void barium$onClose(CallbackInfo ci) {
        // Quando a tela é fechada, notifica o otimizador para limpar o cache associado a este handler.
        if (this.handler != null) {
            InventoryOptimizer.onScreenClosed(this.handler);
        }
    }
}