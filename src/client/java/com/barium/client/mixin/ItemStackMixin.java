package com.barium.client.mixin;

import com.barium.client.optimization.EntityOptimizer;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public class ItemStackMixin {

    // Injetamos em hasGlint para controlá-lo dinamicamente.
    @Inject(method = "hasGlint", at = @At("HEAD"), cancellable = true)
    private void barium$cullDroppedItemGlint(CallbackInfoReturnable<Boolean> cir) {
        // Usamos nosso marcador de contexto. Se não for um item no chão, não fazemos nada.
        // Se for, aplicamos nossa lógica de distância.
        if (!EntityOptimizer.shouldRenderDroppedItemGlint()) {
            cir.setReturnValue(false);
        }
    }
}