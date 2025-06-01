// START OF FILE barium-1.21.5-devs/src/client/java/com/barium/client/mixin/HandledScreenMixin.java
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

    // Declaração de um método shadow para drawSlot.
    // Isso permite que o Mixin chame o método protegido original.
    @Shadow protected abstract void drawSlot(DrawContext context, Slot slot);

    protected HandledScreenMixin() {
        super(null);
    }

    /**
     * Redireciona a chamada para HandledScreen.drawSlot() dentro do método render principal de HandledScreen.
     * Usando nomes ofuscados para garantir a correspondência exata no bytecode.
     *
     * Target Class (obfuscated): net.minecraft.class_465 (HandledScreen)
     * Target Method (obfuscated): method_25426 (render)
     * INVOKE Target (obfuscated): Lnet/minecraft/class_465;method_25396(Lnet/minecraft/class_332;Lnet/minecraft/class_1735;)V
     */
    @Redirect(
        method = "method_25426(Lnet/minecraft/class_332;IIF)V", // Target render method using obfuscated name
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/class_465;method_25396(Lnet/minecraft/class_332;Lnet/minecraft/class_1735;)V" // Target drawSlot call using obfuscated name
        )
    )
    private void barium$redirectDrawSlot(HandledScreen<?> instance, DrawContext context, Slot slot) {
        if (InventoryOptimizer.shouldRedrawSlot(slot)) {
            // Chama o método original drawSlot usando o shadow.
            this.drawSlot(context, slot);
        }
    }

    /**
     * Injeta no final do método close() de HandledScreen para limpar o cache de inventário.
     * Usando nome ofuscado para garantir a correspondência exata.
     *
     * Target Method (obfuscated): method_25402 (close)
     */
    @Inject(
        method = "method_25402()V", // Target close method using obfuscated name
        at = @At("TAIL")
    )
    private void barium$onClose(CallbackInfo ci) {
        if (this.handler != null) {
            InventoryOptimizer.onScreenClosed(this.handler);
        }
    }
}