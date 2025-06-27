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

    // --- OTIMIZAÇÃO DE CACHE (JÁ EXISTENTE, SEM MUDANÇAS) ---

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

    // --- FUNCIONALIDADE: ADICIONAR MARCA DO BARIUM COM ESTILO ---

    /**
     * Intercepta a lista de strings do lado direito do F3 e adiciona
     * as informações do Barium com cores e formatação.
     * Esta funcionalidade agora é incondicional e sempre ativa.
     */
    @ModifyVariable(
        method = "getRightText",
        at = @At(value = "RETURN"),
        ordinal = 0
    )
    private List<String> barium$addStyledRendererInfo(List<String> list) {
        // A checagem da config foi removida. O código abaixo sempre será executado.

        // Adiciona uma linha em branco para separar do conteúdo vanilla.
        list.add("");
        
        // Linha principal com formatação dupla
        list.add(Formatting.AQUA + "Barium" + Formatting.GRAY + " Renderer");

        // Adiciona informações dinâmicas sobre as otimizações ativas com status colorido.
        list.add(formatOption("Vis-Graph Culling", BariumConfig.C.ENABLE_VISIBILITY_GRAPH_CULLING));
        list.add(formatOption("Entity Culling", BariumConfig.C.ENABLE_ENTITY_CULLING));
        list.add(formatOption("Block Entity Culling", BariumConfig.C.ENABLE_BLOCK_ENTITY_OCCLUSION_CULLING));
        list.add(formatOption("Particle Culling", BariumConfig.C.ENABLE_PARTICLE_OPTIMIZATION));

        return list; // Retorna a lista modificada.
    }

    /**
     * Helper method para formatar uma linha de opção com status ON/OFF colorido.
     * @param name O nome da otimização.
     * @param enabled Se ela está ativa ou não.
     * @return A string formatada.
     */
    private String formatOption(String name, boolean enabled) {
        String status = enabled
            ? Formatting.GREEN + "ON"
            : Formatting.RED + "OFF";
        
        return Formatting.DARK_GRAY + " > " + Formatting.WHITE + name + ": " + status;
    }
}