package com.barium.client.mixin;

import com.barium.client.optimization.HudOptimizer;
import com.barium.config.BariumConfig;
import com.google.common.collect.Lists;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.DebugHud;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(DebugHud.class)
public abstract class DebugHudMixin {

    @Shadow @Final private MinecraftClient client;

    // --- LÓGICA DE CACHE E MODIFICAÇÃO PARA O HUD DE DEBUG (F3) ---
    // CORREÇÃO: Os métodos getLeftText e getRightText foram removidos.
    // A nova abordagem usa @ModifyVariable para interceptar as listas de texto
    // diretamente no método render, que é onde elas são criadas agora.

    // Intercepta a lista do lado ESQUERDO (ordinal = 0)
    @ModifyVariable(method = "render(Lnet/minecraft/client/gui/DrawContext;)V", at = @At(value = "STORE"), ordinal = 0)
    private List<String> barium$modifyAndCacheLeftText(List<String> originalList) {
        if (!BariumConfig.C.CACHE_DEBUG_HUD) {
            return originalList; // Se o cache estiver desligado, retorna a lista original sem modificação.
        }

        // Se não devemos recalcular, simplesmente retornamos a lista que já está em cache.
        if (!HudOptimizer.shouldRecalculateDebugHud("debug_left")) {
            return HudOptimizer.getCachedDebugHudText("debug_left");
        }

        // Se devemos recalcular, atualizamos o cache com a nova lista e a retornamos.
        HudOptimizer.updateDebugHudCache("debug_left", originalList);
        return originalList;
    }

    /**
     * O F3 aceita uma atualização ligeiramente menos frequente.
     * Quando o cache está ativo, renderizamos em metade da taxa para reduzir
     * custo de reordenação/bidi de texto.
     */
    @Inject(method = "render(Lnet/minecraft/client/gui/DrawContext;)V", at = @At("HEAD"), cancellable = true)
    private void barium$throttleDebugHudRender(DrawContext context, CallbackInfo ci) {
        if (!BariumConfig.C.CACHE_DEBUG_HUD) return;
        if (client == null || client.world == null) return;

        if ((client.world.getTime() & 1L) != 0L) {
            ci.cancel();
        }
    }

    // Intercepta a lista do lado DIREITO (ordinal = 1)
    @ModifyVariable(method = "render(Lnet/minecraft/client/gui/DrawContext;)V", at = @At(value = "STORE"), ordinal = 1)
    private List<String> barium$modifyAndCacheRightText(List<String> originalList) {
        // ETAPA 1: ADICIONAR A MARCA E O ESTILO
        // Adiciona uma linha em branco para separar do conteúdo vanilla.
        originalList.add("");
        // Linha principal com formatação dupla
        originalList.add(Formatting.AQUA + "Barium" + Formatting.GRAY + " Renderer");
        // Adiciona informações dinâmicas sobre as otimizações ativas com status colorido.
        originalList.add(formatOption("Vis-Graph Culling", BariumConfig.C.ENABLE_VISIBILITY_GRAPH_CULLING));
        originalList.add(formatOption("Entity Culling", BariumConfig.C.ENABLE_ENTITY_CULLING));
        originalList.add(formatOption("Block Entity Culling", BariumConfig.C.ENABLE_BLOCK_ENTITY_OCCLUSION_CULLING));
        originalList.add(formatOption("Particle Culling", BariumConfig.C.ENABLE_PARTICLE_OPTIMIZATION));

        // ETAPA 2: ATUALIZAR O CACHE COM A LISTA JÁ MODIFICADA
        if (BariumConfig.C.CACHE_DEBUG_HUD) {
            // Se não devemos recalcular, retornamos a lista em cache (que já contém a marca).
            if (!HudOptimizer.shouldRecalculateDebugHud("debug_right")) {
                return HudOptimizer.getCachedDebugHudText("debug_right");
            }
            // Se devemos, atualizamos o cache com a nova lista modificada.
            HudOptimizer.updateDebugHudCache("debug_right", originalList);
        }

        // ETAPA 3: RETORNAR A LISTA FINAL
        return originalList;
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
