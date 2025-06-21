// --- Substitua o conteúdo em: src/client/java/com/barium/client/mixin/FogRendererMixin.java ---
package com.barium.client.mixin;

import com.barium.config.BariumConfig;
// --- Imports Corrigidos ---
import net.minecraft.client.MinecraftClient; // Necessário para getInstance()
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.fog.FogRenderer;
// Import corrgido para ClientWorld
import net.minecraft.client.world.ClientWorld; 
// Import JOML para Vector4f
import org.joml.Vector4f; 
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
// Import corrgido para CallbackInfoReturnable
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable; 

@Mixin(FogRenderer.class)
public abstract class FogRendererMixin {

    // --- Campos @Shadow ---
    // Mantidos comentados, pois os tipos referenciados (FogDensityFunction) podem não ser encontrados.
    // @Shadow private FogDensityFunction fogFunction;
    // @Shadow private BlockPos fogPos;

    /**
     * Tenta otimizar a névoa interceptando o retorno do método applyFog e modificando os valores.
     *
     * Assinatura corrigida com base nos mapeamentos Yarn 'named':
     * applyFog(Lnet/minecraft/client/render/Camera;ILnet/minecraft/client/render/RenderTickCounter;FLnet/minecraft/client/world/ClientWorld;Z)Lnet/minecraft/util/math/Vector4f;
     *
     * Parâmetros (ordem corrigida): Camera, int viewDistance, RenderTickCounter tickCounter, float skyDarkness, ClientWorld world, boolean thick
     * Retorno: Vector4f
     *
     * !!! ATENÇÃO: Se o erro "Cannot find target method" persistir APÓS esta correção,
     * !!! significa que a assinatura do método alvo é DIFERENTE do que esperávamos.
     * !!! Precisaremos verificar os mapeamentos exatos do Yarn 1.21.6 para FogRenderer.applyFog.
     */
    @Inject(method = "applyFog(Lnet/minecraft/client/render/Camera;ILnet/minecraft/client/render/RenderTickCounter;FLnet/minecraft/client/world/ClientWorld;Z)Lnet/minecraft/util/math/Vector4f;",
            at = @At("RETURN"), // Injete no RETURN para obter o valor retornado e modificá-lo
            cancellable = true)
    // O retorno do método Java DEVE ser CallbackInfoReturnable<Vector4f>
    private CallbackInfoReturnable<Vector4f> barium$optimizeFogReturn(CallbackInfoReturnable<Vector4f> cir, Camera camera, int viewDistance, boolean thick, RenderTickCounter tickCounter, float skyDarkness, ClientWorld world) {
        
        // Verifica se a otimização geral de névoa está ativada
        if (!BariumConfig.C.ENABLE_FOG_OPTIMIZATION) {
            return cir; 
        }

        // Se a névoa deve ser completamente desativada
        if (BariumConfig.C.DISABLE_FOG) {
            return cir.setReturnValue(new Vector4f(0.0f, 0.0f, 0.0f, 0.0f));
        }

        // Se a otimização de distância está ativa:
        Vector4f originalFogValues = cir.getReturnValue();
        if (originalFogValues == null) {
            return cir; 
        }

        // Obtém a render distance do jogo em blocos.
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.options == null) {
             return cir; 
        }
        int playerRenderDistanceInChunks = client.options.getViewDistance().getValue();
        float baseRenderDistanceInBlocks = (float)playerRenderDistanceInChunks * 16.0f;

        // Calcula o novo valor de start da névoa.
        float newFogStart = baseRenderDistanceInBlocks * (BariumConfig.C.FOG_START_PERCENTAGE / 100.0f);

        // Modifica o Vector4f retornado. Assumimos que x é start, y é end.
        Vector4f modifiedFogValues = new Vector4f(newFogStart, originalFogValues.y, originalFogValues.z, originalFogValues.w);
        
        return cir.setReturnValue(modifiedFogValues);
    }
}