// --- Substitua o conteúdo em: src/client/java/com/barium/client/mixin/FogRendererMixin.java ---
package com.barium.client.mixin;

import com.barium.config.BariumConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.fog.FogRenderer;
import net.minecraft.client.world.ClientWorld; 
import org.joml.Vector4f; 
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FogRenderer.class)
public abstract class FogRendererMixin {

    /**
     * Tenta otimizar a névoa interceptando e modificando o valor retornado por applyFog.
     * Assinatura corrigida com base nos mapeamentos Yarn 'named':
     * applyFog(Lnet/minecraft/client/render/Camera;ILnet/minecraft/client/render/RenderTickCounter;FLnet/minecraft/client/world/ClientWorld;Z)Lnet/minecraft/util/math/Vector4f;
     *
     * Retorno do método Java: Vector4f
     */
    @Inject(method = "applyFog", at = @At(value = "RETURN"), cancellable = true)
    // O RETORNO DO MÉTODO JAVA DEVE SER o mesmo do método alvo, e recebemos o retorno original como parâmetro.
    private Vector4f barium$optimizeFogReturn(Vector4f originalFogValues, Camera camera, int viewDistance, RenderTickCounter tickCounter, float skyDarkness, ClientWorld world, boolean thick) {
        
        // Verifica se a otimização geral de névoa está ativada
        if (!BariumConfig.C.ENABLE_FOG_OPTIMIZATION) {
            return originalFogValues; // Retorna o valor original se a otimização geral estiver desativada
        }

        // Se a névoa deve ser completamente desativada
        if (BariumConfig.C.DISABLE_FOG) {
            // Retorna um novo Vector4f que tenta desativar a névoa.
            return new Vector4f(0.0f, 0.0f, 0.0f, 0.0f);
        }

        // Se a otimização de distância está ativa:
        if (originalFogValues == null) {
            return originalFogValues; // Segurança: se o valor retornado for nulo, não faz nada.
        }

        // Obtém a render distance do jogo em blocos.
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.options == null) {
             return originalFogValues; // Segurança: se algo não estiver inicializado, retorna o original.
        }
        int playerRenderDistanceInChunks = client.options.getViewDistance().getValue();
        float baseRenderDistanceInBlocks = (float)playerRenderDistanceInChunks * 16.0f;

        // Calcula o novo valor de start da névoa.
        float newFogStart = baseRenderDistanceInBlocks * (BariumConfig.C.FOG_START_PERCENTAGE / 100.0f);

        // Modifica o Vector4f retornado. Assumimos que x é start, y é end.
        Vector4f modifiedFogValues = new Vector4f(newFogStart, originalFogValues.y, originalFogValues.z, originalFogValues.w);
        
        return modifiedFogValues;
    }
}