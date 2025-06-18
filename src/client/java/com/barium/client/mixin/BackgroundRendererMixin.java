package com.barium.client.mixin;

import com.barium.config.BariumConfig;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BackgroundRenderer.class)
public class BackgroundRendererMixin {

    /**
     * Injeta no início do método que aplica a névoa para assumir o controle.
     * Isso nos permite desativar ou modificar a distância da névoa com base na configuração.
     */
    @Inject(
        method = "applyFog(Lnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/BackgroundRenderer$FogType;FZZ)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private static void barium$advancedFogControl(Camera camera, BackgroundRenderer.FogType fogType, float viewDistance, boolean thickFog, boolean forceThickFog, CallbackInfo ci) {
        // Só aplica nossa lógica para a névoa de terreno (a principal).
        if (fogType != BackgroundRenderer.FogType.TERRAIN) {
            return;
        }

        // Se a otimização estiver desativada, não faz nada e deixa o método original rodar.
        if (!BariumConfig.C.ENABLE_FOG_OPTIMIZATION) {
            return;
        }

        float fogStart;
        float fogEnd;

        if (BariumConfig.C.DISABLE_FOG) {
            // Desativa a névoa completamente, empurrando-a para o limite da render distance.
            fogStart = viewDistance * 2.0f; // Um valor bem alto
            fogEnd = viewDistance * 2.5f;   // Um valor ainda mais alto
        } else {
            // Modifica a distância inicial da névoa com base na porcentagem da config.
            float startPercentage = BariumConfig.C.FOG_START_PERCENTAGE / 100.0f;
            fogStart = viewDistance * startPercentage;
            fogEnd = viewDistance; // A névoa termina no final da render distance.
        }
        
        // Define os novos valores de névoa no RenderSystem
        RenderSystem.setShaderFogStart(fogStart);
        RenderSystem.setShaderFogEnd(fogEnd);

        // Cancela o resto do método original para impedir que ele sobrescreva nossos valores.
        ci.cancel();
    }
}