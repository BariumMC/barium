package com.barium.client.mixin;

import com.barium.config.BariumConfig;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.FogShape;
import net.minecraft.client.render.RenderTickCounter; // Import necessário
import net.minecraft.client.render.fog.FogRenderer;
import net.minecraft.client.world.ClientWorld; // Import necessário
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FogRenderer.class)
public class FogRendererMixin {

    /**
     * Injeta ANTES da chamada a RenderSystem.setShaderFogColor, que é o ponto onde
     * a névoa é efetivamente aplicada. Isso nos permite modificar os parâmetros de distância
     * antes que eles sejam usados.
     * 
     * O método alvo é applyFog, mas em vez de injetar no HEAD, vamos injetar
     * logo antes de a cor da névoa ser definida, para garantir que temos os dados corretos.
     */
    @Inject(
        // Este é o método correto que você encontrou.
        method = "applyFog(Lnet/minecraft/client/render/Camera;IZLnet/minecraft/client/render/RenderTickCounter;FLnet/minecraft/client/world/ClientWorld;)Lorg/joml/Vector4f;",
        at = @At(
            value = "INVOKE",
            target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShaderFogColor(FFFF)V"
        ),
        cancellable = true // Ainda usamos cancellable para ter a opção de pular o setShaderFogColor se necessário
    )
    private static void barium$advancedFogControl(Camera camera, int viewDistance, boolean thickFog, RenderTickCounter tickCounter, float skyDarkness, ClientWorld world, CallbackInfo ci) {
        if (!BariumConfig.C.ENABLE_FOG_OPTIMIZATION) {
            return;
        }

        float fogStart;
        float fogEnd;
        float effectiveViewDistance = viewDistance; // O parâmetro 'viewDistance' já é o que precisamos

        if (BariumConfig.C.DISABLE_FOG) {
            // Desativa a névoa empurrando-a para muito longe
            fogStart = effectiveViewDistance * 2.0f;
            fogEnd = effectiveViewDistance * 2.5f;
        } else {
            // Usa a porcentagem da configuração para definir o início
            float startPercentage = BariumConfig.C.FOG_START_PERCENTAGE / 100.0f;
            fogStart = effectiveViewDistance * startPercentage;
            fogEnd = effectiveViewDistance;
        }
        
        // Define os novos parâmetros de distância da névoa
        RenderSystem.setShaderFogStart(fogStart);
        RenderSystem.setShaderFogEnd(fogEnd);
        RenderSystem.setShaderFogShape(FogShape.SPHERE);
        
        // Não cancelamos o método aqui, pois queremos que o RenderSystem.setShaderFogColor
        // original seja chamado depois, mas com nossos valores de distância já definidos.
        // Se quiséssemos também mudar a cor, poderíamos usar ci.cancel() e chamar
        // RenderSystem.setShaderFogColor com nossa própria cor.
    }
}