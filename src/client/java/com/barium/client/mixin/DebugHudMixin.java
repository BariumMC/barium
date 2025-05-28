package com.barium.client.mixin;

import com.barium.client.optimization.HudOptimizer;
import net.minecraft.client.gui.hud.DebugHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

/**
 * Mixin para DebugHud para otimizar a obtenção e renderização das informações de debug (F3).
 * Revisado para compatibilidade com mappings Yarn 1.21.5+build.1.
 */
@Mixin(DebugHud.class)
public abstract class DebugHudMixin {

    /**
     * Injeta no início do método getLeftText para potencialmente retornar texto do cache.
     * A lógica de otimização real depende da implementação em HudOptimizer.
     *
     * Target Class: net.minecraft.client.gui.hud.DebugHud
     * Target Method Signature (Yarn 1.21.5+build.1): getLeftText()Ljava/util/List;
     */
    @Inject(
        method = "getLeftText()Ljava/util/List;", // Confirmado com Yarn 1.21.5+build.1
        at = @At("HEAD"),
        cancellable = true
    )
    private void barium$getLeftText(CallbackInfoReturnable<List<String>> cir) {
        DebugHud self = (DebugHud)(Object)this;
        // Tenta obter o texto do cache do HudOptimizer
        List<String> cachedText = HudOptimizer.getCachedDebugHudText(self, "left");

        // Se o cache estiver válido (não vazio), retorna o cache.
        // A implementação atual do HudOptimizer pode precisar de revisão para funcionar corretamente.
        if (cachedText != null && !cachedText.isEmpty()) {
            // TODO: Validar e refinar a lógica de cache em HudOptimizer.
            // Se o cache for confiável, descomentar a linha abaixo:
            // cir.setReturnValue(cachedText);
        }
        // Se o cache estiver vazio ou nulo, permite que o método original execute para gerar o texto.
        // O HudOptimizer deve ser responsável por atualizar o cache em outro ponto (ex: no final do método original).
    }

    /**
     * Injeta no início do método getRightText para potencialmente retornar texto do cache.
     * A lógica de otimização real depende da implementação em HudOptimizer.
     *
     * Target Class: net.minecraft.client.gui.hud.DebugHud
     * Target Method Signature (Yarn 1.21.5+build.1): getRightText()Ljava/util/List;
     */
    @Inject(
        method = "getRightText()Ljava/util/List;", // Confirmado com Yarn 1.21.5+build.1
        at = @At("HEAD"),
        cancellable = true
    )
    private void barium$getRightText(CallbackInfoReturnable<List<String>> cir) {
        DebugHud self = (DebugHud)(Object)this;
        // Tenta obter o texto do cache do HudOptimizer
        List<String> cachedText = HudOptimizer.getCachedDebugHudText(self, "right");

        // Mesma lógica do getLeftText
        if (cachedText != null && !cachedText.isEmpty()) {
            // TODO: Validar e refinar a lógica de cache em HudOptimizer.
            // Se o cache for confiável, descomentar a linha abaixo:
            // cir.setReturnValue(cachedText);
        }
        // Se o cache estiver vazio ou nulo, permite que o método original execute.
    }

    // TODO: Considerar injeções adicionais (ex: @Inject no final de getLeftText/getRightText)
    //       para atualizar o cache no HudOptimizer após o texto ser gerado pelo método original.
}

