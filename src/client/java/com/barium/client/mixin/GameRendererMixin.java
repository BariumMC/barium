package com.barium.client.mixin;

import com.barium.config.BariumConfig;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter; // Importe RenderTickCounter
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

    @Shadow public abstract void onResized(int width, int height);
    
    // CORREÇÃO: O método é abstrato na classe GameRenderer, precisamos da sombra.
    @Shadow public abstract float getRenderDistance();

    /**
     * Injeta ANTES da chamada a 'setupWorldFog', que é o local ideal para definir nossos próprios valores.
     * A assinatura do método renderWorld foi atualizada para 1.21.6.
     */
    @Inject(
        method = "renderWorld(Lnet/minecraft/client/render/RenderTickCounter;FJLnet/minecraft/client/util/math/MatrixStack;)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/GameRenderer;setupWorldFog(Lnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/FogType;FZ)V")
    )
    private void barium$advancedFogControl(RenderTickCounter tickCounter, float tickDelta, long limitTime, MatrixStack matrices, CallbackInfo ci) {
        if (!BariumConfig.C.ENABLE_FOG_OPTIMIZATION) {
            return;
        }
        
        // CORREÇÃO: O método correto é getRenderDistance().
        float renderDistance = this.getRenderDistance();
        
        float fogStart;
        float fogEnd;

        if (BariumConfig.C.DISABLE_FOG) {
            fogStart = renderDistance; // Inicia no final da render distance
            fogEnd = renderDistance * 1.1f; // Termina um pouco depois
        } else {
            float startPercentage = BariumConfig.C.FOG_START_PERCENTAGE / 100.0f;
            fogStart = renderDistance * startPercentage;
            fogEnd = renderDistance;
        }
        
        // CORREÇÃO: Os métodos corretos para definir a névoa.
        RenderSystem.setShaderFogStart(fogStart);
        RenderSystem.setShaderFogEnd(fogEnd);
        // O tipo de névoa (linear, exp) é definido pelo Minecraft em 'setupWorldFog',
        // nós apenas ajustamos as distâncias antes disso.
    }
    
    @Inject(
        method = "onResized(II)V",
        at = @At("RETURN")
    )
    private void barium$forceResizeEntityOutlineFramebuffer(int width, int height, CallbackInfo ci) {
        if (BariumConfig.C.ENABLE_HALF_RESOLUTION_ENTITY_OUTLINES) {
            Framebuffer entityOutlinesFramebuffer = this.client.worldRenderer.getEntityOutlinesFramebuffer();
            
            if (entityOutlinesFramebuffer != null) {
                // CORREÇÃO: A assinatura de resize não tem mais o booleano do Mac.
                entityOutlinesFramebuffer.resize(width / 2, height / 2);
            }
        }
    }
}