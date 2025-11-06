package com.barium.client.mixin;

import com.barium.client.optimization.HudOptimizer;
import com.barium.config.BariumConfig;
import net.minecraft.client.gui.hud.DebugHud;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(DebugHud.class)
public abstract class DebugHudMixin {

    // --- LADO ESQUERDO DO F3 (CACHE) ---
    @Inject(method = "getLeftText(Ljava/util/List;)V", at = @At("HEAD"), cancellable = true)
    private void barium$getLeftTextHead(List<String> text, CallbackInfo ci) {
        if (!HudOptimizer.shouldRecalculateDebugHud("debug_left")) {
            text.addAll(HudOptimizer.getCachedDebugHudText("debug_left"));
            ci.cancel();
        }
    }

    @Inject(method = "getLeftText(Ljava/util/List;)V", at = @At("TAIL"))
    private void barium$getLeftTextReturn(List<String> text, CallbackInfo ci) {
        HudOptimizer.updateDebugHudCache("debug_left", text);
    }

    // --- LADO DIREITO DO F3 (CACHE + BRANDING) ---
    @Inject(method = "getRightText(Ljava/util/List;)V", at = @At("HEAD"), cancellable = true)
    private void barium$getRightTextHead(List<String> text, CallbackInfo ci) {
        if (BariumConfig.C.CACHE_DEBUG_HUD && !HudOptimizer.shouldRecalculateDebugHud("debug_right")) {
            text.addAll(HudOptimizer.getCachedDebugHudText("debug_right"));
            ci.cancel();
        }
    }

    @Inject(method = "getRightText(Ljava/util/List;)V", at = @At("TAIL"))
    private void barium$addBrandingAndCache(List<String> text, CallbackInfo ci) {
        // ETAPA 1: ADICIONAR A MARCA E O ESTILO
        text.add("");
        text.add(Formatting.AQUA + "Barium" + Formatting.GRAY + " Renderer");
        text.add(formatOption("Vis-Graph Culling", BariumConfig.C.ENABLE_VISIBILITY_GRAPH_CULLING));
        text.add(formatOption("Entity Culling", BariumConfig.C.ENABLE_ENTITY_CULLING));
        text.add(formatOption("Block Entity Culling", BariumConfig.C.ENABLE_BLOCK_ENTITY_OCCLUSION_CULLING));
        text.add(formatOption("Particle Culling", BariumConfig.C.ENABLE_PARTICLE_OPTIMIZATION));

        // ETAPA 2: ATUALIZAR O CACHE COM A LISTA JÁ MODIFICADA
        if (BariumConfig.C.CACHE_DEBUG_HUD) {
            HudOptimizer.updateDebugHudCache("debug_right", text);
        }
    }

    private String formatOption(String name, boolean enabled) {
        String status = enabled
            ? Formatting.GREEN + "ON"
            : Formatting.RED + "OFF";
        
        return Formatting.DARK_GRAY + " > " + Formatting.WHITE + name + ": " + status;
    }
}