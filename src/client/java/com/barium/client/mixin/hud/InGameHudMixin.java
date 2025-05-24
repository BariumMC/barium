package com.barium.client.mixin.hud;

import com.barium.config.BariumConfig;
import com.barium.client.optimization.HudStateTracker; // Importa a classe HudStateTracker
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
// import org.spongepowered.asm.mixin.injection.callback.LocalVariablesCapture; // REMOVER ESTE IMPORT: não é mais necessário com locals = {}

import com.barium.client.mixin.accessor.MinecraftClientAccessor; // Importa o Accessor para MinecraftClient

// Import para PlayerInventoryAccessor (APENAS SE O ERRO 'selectedSlot' PERSISTIR em HudStateTracker.java)
// import com.barium.client.mixin.accessor.PlayerInventoryAccessor; 

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {

    @Shadow @Final private MinecraftClient client;
    @Shadow public abstract TextRenderer getTextRenderer();

    /**
     * Injeta no método `tick()` para atualizar o estado do jogador e as flags de dirty.
     * `tick()` é chamado a cada tick de jogo.
     * @param ci CallbackInfo.
     */
    @Inject(method = "tick", at = @At("TAIL"))
    private void barium$onTickUpdateState(CallbackInfo ci) {
        if (BariumConfig.getInstance().HUD_OPTIMIZATIONS.ENABLE_DIRTY_FLAG_OPTIMIZATION && this.client.player != null) {
            HudStateTracker.updatePlayerState(this.client.player); 
        }
    }

    // --- Dirty Flag Optimizations para barras de HUD ---
    // Injeções para cancelar a renderização se a flag de dirty não estiver ativa.
    // As flags são marcadas como "limpas" após a renderização.

    @Inject(method = "renderHealth", at = @At("HEAD"), cancellable = true)
    private void barium$cancellableRenderHealth(DrawContext context, PlayerEntity player, CallbackInfo ci) {
        if (BariumConfig.getInstance().HUD_OPTIMIZATIONS.ENABLE_DIRTY_FLAG_OPTIMIZATION && !HudStateTracker.healthDirty) {
            ci.cancel();
        }
    }

    @Inject(method = "renderHealth", at = @At("TAIL"))
    private void barium$markHealthClean(DrawContext context, PlayerEntity player, CallbackInfo ci) {
        if (BariumConfig.getInstance().HUD_OPTIMIZATIONS.ENABLE_DIRTY_FLAG_OPTIMIZATION) {
            HudStateTracker.healthDirty = false;
        }
    }

    @Inject(method = "renderFood", at = @At("HEAD"), cancellable = true)
    private void barium$cancellableRenderFood(DrawContext context, PlayerEntity player, CallbackInfo ci) {
        if (BariumConfig.getInstance().HUD_OPTIMIZATIONS.ENABLE_DIRTY_FLAG_OPTIMIZATION && !HudStateTracker.foodDirty) {
            ci.cancel();
        }
    }

    @Inject(method = "renderFood", at = @At("TAIL"))
    private void barium$markFoodClean(DrawContext context, PlayerEntity player, CallbackInfo ci) {
        if (BariumConfig.getInstance().HUD_OPTIMIZATIONS.ENABLE_DIRTY_FLAG_OPTIMIZATION) {
            HudStateTracker.foodDirty = false;
        }
    }

    @Inject(method = "renderArmor", at = @At("HEAD"), cancellable = true)
    private void barium$cancellableRenderArmor(DrawContext context, PlayerEntity player, CallbackInfo ci) {
        if (BariumConfig.getInstance().HUD_OPTIMIZATIONS.ENABLE_DIRTY_FLAG_OPTIMIZATION && !HudStateTracker.armorDirty) {
            ci.cancel();
        }
    }

    @Inject(method = "renderArmor", at = @At("TAIL"))
    private void barium$markArmorClean(DrawContext context, PlayerEntity player, CallbackInfo ci) {
        if (BariumConfig.getInstance().HUD_OPTIMIZATIONS.ENABLE_DIRTY_FLAG_OPTIMIZATION) {
            HudStateTracker.armorDirty = false;
        }
    }

    @Inject(method = "renderExperienceBar", at = @At("HEAD"), cancellable = true)
    private void barium$cancellableRenderExperienceBar(DrawContext context, int x, PlayerEntity player, CallbackInfo ci) {
        if (BariumConfig.getInstance().HUD_OPTIMIZATIONS.ENABLE_DIRTY_FLAG_OPTIMIZATION && !HudStateTracker.experienceDirty) {
            ci.cancel();
        }
    }

    @Inject(method = "renderExperienceBar", at = @At("TAIL"))
    private void barium$markExperienceClean(DrawContext context, int x, PlayerEntity player, CallbackInfo ci) {
        if (BariumConfig.getInstance().HUD_OPTIMIZATIONS.ENABLE_DIRTY_FLAG_OPTIMIZATION) {
            HudStateTracker.experienceDirty = false;
        }
    }
    
    // Otimização da hotbar: cancela renderização se não for dirty
    @Inject(method = "renderHotbar", at = @At("HEAD"), cancellable = true)
    private void barium$cancellableRenderHotbar(float tickDelta, DrawContext context, CallbackInfo ci) {
        if (BariumConfig.getInstance().HUD_OPTIMIZATIONS.ENABLE_DIRTY_FLAG_OPTIMIZATION && !HudStateTracker.hotbarSelectionDirty) {
            ci.cancel();
        }
    }

    @Inject(method = "renderHotbar", at = @At("TAIL"))
    private void barium$markHotbarClean(float tickDelta, DrawContext context, CallbackInfo ci) {
        if (BariumConfig.getInstance().HUD_OPTIMIZATIONS.ENABLE_DIRTY_FLAG_OPTIMIZATION) {
            HudStateTracker.hotbarSelectionDirty = false;
        }
    }

    // --- Batching e Profiling de Texto ---

    // Modifica o VertexConsumerProvider usado por TextRenderer.draw para garantir batching centralizado.
    // O DrawContext já faz um bom trabalho, mas isso reforça e permite profiling.
    @ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/font/TextRenderer;draw(Lnet/minecraft/text/Text;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/client/font/TextRenderer$TextLayerType;II)I"), index = 6)
    private VertexConsumerProvider barium$modifyTextRendererDrawProvider(VertexConsumerProvider originalProvider) {
        if (BariumConfig.getInstance().HUD_OPTIMIZATIONS.ENABLE_CENTRALIZED_TEXT_BATCHING) {
            return originalProvider; // Retorna o provider padrão do DrawContext, que é flusheado no final.
        }
        return originalProvider;
    }

    // Injeta antes de uma chamada de `TextRenderer.draw` para iniciar uma seção no profiler.
    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/font/TextRenderer;draw(Lnet/minecraft/text/Text;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/client/font/TextRenderer$TextLayerType;II)I", shift = At.Shift.BEFORE))
           // locals = {}) // CORREÇÃO: Usar array vazio para locals = {}
    private void barium$profileTextRenderStart(DrawContext context, float tickDelta, CallbackInfo ci) {
        if (BariumConfig.getInstance().TEXT_RENDERING_OPTIMIZATIONS.ENABLE_TEXT_PROFILING) {
            // CORREÇÃO: Usar o accessor para acessar o profiler
            ((MinecraftClientAccessor)this.client).getProfiler_().push("barium_ingame_hud_text_render");
        }
    }

    // Injeta depois de uma chamada de `TextRenderer.draw` para finalizar a seção do profiler.
    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/font/TextRenderer;draw(Lnet/minecraft/text/Text;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/client/font/TextRenderer$TextLayerType;II)I", shift = At.Shift.AFTER))
          //  locals = {}) // CORREÇÃO: Usar array vazio para locals = {}
    private void barium$profileTextRenderEnd(DrawContext context, float tickDelta, CallbackInfo ci) {
        if (BariumConfig.getInstance().TEXT_RENDERING_OPTIMIZATIONS.ENABLE_TEXT_PROFILING) {
            // CORREÇÃO: Usar o accessor para acessar o profiler
            ((MinecraftClientAccessor)this.client).getProfiler_().pop();
        }
    }
}