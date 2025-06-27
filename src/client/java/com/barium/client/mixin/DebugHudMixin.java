package com.barium.client.mixin;

import com.barium.client.optimization.HudOptimizer;
import com.barium.config.BariumConfig;
import net.minecraft.client.gui.hud.DebugHud;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(DebugHud.class)
public abstract class DebugHudMixin {

    // --- LADO ESQUERDO DO F3 (CACHE) ---
    // Esta parte não precisa de modificação e pode continuar como está.
    @Inject(method = "getLeftText", at = @At("HEAD"), cancellable = true)
    private void barium$getLeftTextHead(CallbackInfoReturnable<List<String>> cir) {
        if (!HudOptimizer.shouldRecalculateDebugHud("debug_left")) {
            cir.setReturnValue(HudOptimizer.getCachedDebugHudText("debug_left"));
        }
    }

    @Inject(method = "getLeftText", at = @At("RETURN"))
    private void barium$getLeftTextReturn(CallbackInfoReturnable<List<String>> cir) {
        HudOptimizer.updateDebugHudCache("debug_left", cir.getReturnValue());
    }


    // --- LADO DIREITO DO F3 (CACHE + BRANDING) ---
    // A lógica foi redesenhada para ser 100% robusta.

    // 1. Otimização de Cache: Roda primeiro e sai rápido se possível.
    @Inject(method = "getRightText", at = @At("HEAD"), cancellable = true)
    private void barium$getRightTextHead(CallbackInfoReturnable<List<String>> cir) {
        // Se a otimização de cache estiver desligada, não fazemos nada aqui.
        if (!BariumConfig.C.CACHE_DEBUG_HUD) return;

        if (!HudOptimizer.shouldRecalculateDebugHud("debug_right")) {
            cir.setReturnValue(HudOptimizer.getCachedDebugHudText("debug_right"));
        }
    }

    // 2. Modificação e Atualização do Cache: Roda APENAS se o cache precisar ser recalculado.
    @ModifyVariable(
        method = "getRightText",
        at = @At(value = "STORE"), // Injeta DEPOIS que a lista é calculada, mas ANTES de ser retornada.
        ordinal = 0
    )
    private List<String> barium$addBrandingAndCache(List<String> list) {
        // ETAPA 1: ADICIONAR A MARCA E O ESTILO
        // Adiciona uma linha em branco para separar do conteúdo vanilla.
        list.add("");
        // Linha principal com formatação dupla
        list.add(Formatting.AQUA + "Barium" + Formatting.GRAY + " Renderer");
        // Adiciona informações dinâmicas sobre as otimizações ativas com status colorido.
        list.add(formatOption("Vis-Graph Culling", BariumConfig.C.ENABLE_VISIBILITY_GRAPH_CULLING));
        list.add(formatOption("Entity Culling", BariumConfig.C.ENABLE_ENTITY_CULLING));
        list.add(formatOption("Block Entity Culling", BariumConfig.C.ENABLE_BLOCK_ENTITY_OCCLUSION_CULLING));
        list.add(formatOption("Particle Culling", BariumConfig.C.ENABLE_PARTICLE_OPTIMIZATION));

        // ETAPA 2: ATUALIZAR O CACHE COM A LISTA JÁ MODIFICADA
        // Só atualiza o cache se a otimização estiver ligada.
        if (BariumConfig.C.CACHE_DEBUG_HUD) {
            HudOptimizer.updateDebugHudCache("debug_right", list);
        }

        // ETAPA 3: RETORNAR A LISTA FINAL
        return list;
    }

    /**
     * Helper method para formatar uma linha de opção com status ON/OFF colorido.
     */
    private String formatOption(String name, boolean enabled) {
        String status = enabled
            ? Formatting.GREEN + "ON"
            : Formatting.RED + "OFF";
        
        return Formatting.DARK_GRAY + " > " + Formatting.WHITE + name + ": " + status;
    }
}