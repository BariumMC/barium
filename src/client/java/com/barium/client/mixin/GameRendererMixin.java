package com.barium.client.mixin;

import com.barium.config.BariumConfig;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

    @Shadow @Final private MinecraftClient client;
    @Shadow public abstract void onResized(int width, int height); // Assumindo que o método original está aqui

    /**
     * Injeta no início do método que renderiza o mundo.
     * Neste ponto, modificamos os parâmetros da névoa ANTES que o Minecraft os configure.
     * Usamos um Redirect para interceptar a chamada específica que define a névoa.
     */
    @Inject(
        method = "renderWorld(FJLnet/minecraft/client/util/math/MatrixStack;)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;update(Lnet/minecraft/world/BlockView;Lnet/minecraft/entity/Entity;ZZF)V")
    )
    private void barium$advancedFogControl(float tickDelta, long limitTime, MatrixStack matrices, CallbackInfo ci) {
        if (!BariumConfig.C.ENABLE_FOG_OPTIMIZATION) {
            return;
        }

        // Obtemos a viewDistance do cliente.
        float viewDistance = this.client.gameRenderer.getViewDistance();
        
        float fogStart;
        float fogEnd;

        if (BariumConfig.C.DISABLE_FOG) {
            // Desativa a névoa completamente
            fogStart = viewDistance * 2.0f;
            fogEnd = viewDistance * 2.5f;
        } else {
            // Modifica a distância inicial com base na porcentagem
            float startPercentage = BariumConfig.C.FOG_START_PERCENTAGE / 100.0f;
            fogStart = viewDistance * startPercentage;
            fogEnd = viewDistance;
        }
        
        // Aplica os novos valores.
        // O tipo de névoa (GL_EXP, GL_EXP2, GL_LINEAR) é definido como LINEAR por padrão aqui.
        RenderSystem.setShaderFogStart(fogStart);
        RenderSystem.setShaderFogEnd(fogEnd);
    }
    
    /**
     * Injeta no final do método onResized para redimensionar o framebuffer de contorno.
     * (Este método já existia e deve ser mantido).
     */
    @Inject(
        method = "onResized(II)V",
        at = @At("RETURN")
    )
    private void barium$forceResizeEntityOutlineFramebuffer(int width, int height, CallbackInfo ci) {
        if (BariumConfig.C.ENABLE_HALF_RESOLUTION_ENTITY_OUTLINES) {
            Framebuffer entityOutlinesFramebuffer = this.client.worldRenderer.getEntityOutlinesFramebuffer();
            
            if (entityOutlinesFramebuffer != null) {
                entityOutlinesFramebuffer.resize(width / 2, height / 2, MinecraftClient.IS_SYSTEM_MAC);
            }
        }
    }
}