package com.barium.client.mixin;

import com.barium.client.optimization.HudOptimizer;
import net.minecraft.client.gui.hud.DebugHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(DebugHud.class)
public abstract class DebugHudMixin {

    // --- OTIMIZAÇÃO DO LADO ESQUERDO DO F3 ---

    // Injeta NO INÍCIO do método que busca o texto esquerdo.
    @Inject(method = "getLeftText", at = @At("HEAD"), cancellable = true)
    private void barium$getLeftTextHead(CallbackInfoReturnable<List<String>> cir) {
        // Se NÃO devemos recalcular (ou seja, o cache é válido)...
        if (!HudOptimizer.shouldRecalculateDebugHud("debug_left")) {
            // ...nós retornamos o texto do cache e cancelamos o método original.
            // O Minecraft nunca chegará a fazer os cálculos caros.
            cir.setReturnValue(HudOptimizer.getCachedDebugHudText("debug_left"));
        }
    }

    // Injeta NO FIM do método. Só será executado se o @At("HEAD") não cancelar.
    @Inject(method = "getLeftText", at = @At("RETURN"))
    private void barium$getLeftTextReturn(CallbackInfoReturnable<List<String>> cir) {
        // Se chegamos aqui, significa que o jogo recalculou o texto.
        // Agora, pegamos esse resultado e o guardamos em nosso cache para a próxima vez.
        HudOptimizer.updateDebugHudCache("debug_left", cir.getReturnValue());
    }

    // --- OTIMIZAÇÃO DO LADO DIREITO DO F3 (mesma lógica) ---

    @Inject(method = "getRightText", at = @At("HEAD"), cancellable = true)
    private void barium$getRightTextHead(CallbackInfoReturnable<List<String>> cir) {
        if (!HudOptimizer.shouldRecalculateDebugHud("debug_right")) {
            cir.setReturnValue(HudOptimizer.getCachedDebugHudText("debug_right"));
        }
    }

    @Inject(method = "getRightText", at = @At("RETURN"))
    private void barium$getRightTextReturn(CallbackInfoReturnable<List<String>> cir) {
        HudOptimizer.updateDebugHudCache("debug_right", cir.getReturnValue());
    }
}