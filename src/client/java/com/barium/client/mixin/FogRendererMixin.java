package com.barium.client.mixin;

import com.barium.config.BariumConfig;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.Camera;
// Não precisamos mais de FogShape
import net.minecraft.client.render.fog.FogRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FogRenderer.class)
public class FogRendererMixin {

    /**
     * Injeta no início do método que configura a névoa para modificar suas distâncias.
     * Esta é a abordagem mais compatível, pois não interfere com a forma ou cor da névoa,
     * apenas com o quão perto ou longe ela começa e termina.
     */
    @Inject(
        // A assinatura do método alvo está correta para 1.21.6.
        method = "setupFog(Lnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/fog/FogRenderer$FogType;FZF)V",
        at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShaderFogStart(F)V"),
        cancellable = true // Usamos cancellable para, se necessário, pular a lógica original de distância.
    )
    private static void barium$advancedFogControl(Camera camera, FogRenderer.FogType fogType, float viewDistance, boolean thickFog, float tickDelta, CallbackInfo ci) {
        // Aplica a otimização apenas para a névoa do terreno.
        if (fogType != FogRenderer.FogType.TERRAIN || !BariumConfig.C.ENABLE_FOG_OPTIMIZATION) {
            return;
        }

        float fogStart;
        float fogEnd;

        if (BariumConfig.C.DISABLE_FOG) {
            // Empurra a névoa para longe para desativá-la.
            fogStart = viewDistance * 2;
            fogEnd = viewDistance * 2.5f;
        } else {
            // Usa a porcentagem da configuração para definir o início da névoa.
            float startPercentage = BariumConfig.C.FOG_START_PERCENTAGE / 100.0f;
            fogStart = viewDistance * startPercentage;
            fogEnd = viewDistance;
        }
        
        // Define os novos parâmetros de início e fim da névoa.
        RenderSystem.setShaderFogStart(fogStart);
        RenderSystem.setShaderFogEnd(fogEnd);

        // Cancela o resto do método original do Minecraft para impedir que ele
        // sobrescreva os valores de distância que acabamos de definir.
        // A cor e a forma da névoa já terão sido definidas antes deste ponto de injeção.
        ci.cancel();
    }
}