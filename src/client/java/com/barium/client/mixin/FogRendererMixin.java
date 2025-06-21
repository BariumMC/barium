// --- Substitua o conteúdo em: src/client/java/com/barium/client/mixin/FogRendererMixin.java ---
package com.barium.client.mixin;

import com.barium.config.BariumConfig;
// --- Imports Corrigidos e Ajustados ---
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderTickCounter;
// Import do FogRenderer já é implícito pelo @Mixin
import net.minecraft.client.render.fog.FogRenderer;
// Import corrgido para ClientWorld
import net.minecraft.client.world.ClientWorld; 
// Import JOML para Vector4f
import org.joml.Vector4f; 
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FogRenderer.class)
public abstract class FogRendererMixin {

    // --- Campos Shadow ---
    // REMOVIDOS: Os campos @Shadow foram removidos pois referenciam tipos que ainda não foram resolvidos
    // ou que o Mixin não consegue encontrar. Se FogDensityFunction for necessário,
    // sua localização ou substituição precisará ser investigada.
    // @Shadow private FogDensityFunction fogFunction;
    // @Shadow private BlockPos fogPos;

    /**
     * Tenta otimizar a névoa interceptando o retorno do método applyFog e modificando os valores.
     *
     * Assinatura corrigida com base nos mapeamentos Yarn 'named':
     * applyFog(Lnet/minecraft/client/render/Camera;ILnet/minecraft/client/render/RenderTickCounter;FLnet/minecraft/client/world/ClientWorld;Z)Lnet/minecraft/util/math/Vector4f;
     *
     * Parâmetros:
     * 0: Camera camera
     * 1: int viewDistance
     * 2: boolean thick  <-- Note que 'thick' vem antes de 'skyDarkness'
     * 3: RenderTickCounter tickCounter
     * 4: float skyDarkness
     * 5: ClientWorld world
     * Retorno: Vector4f
     *
     * !!! ATENÇÃO: Se o erro "Cannot find target method" persistir APÓS esta correção,
     * !!! significa que a assinatura do método alvo (ou o próprio método) no Yarn 1.21.6
     * !!! é diferente do que esperávamos. Investigar o refmap.json ou os mapeamentos Yarn será necessário.
     */
    @Inject(method = "applyFog(Lnet/minecraft/client/render/Camera;ILnet/minecraft/client/render/RenderTickCounter;FLnet/minecraft/client/world/ClientWorld;Z)Lnet/minecraft/util/math/Vector4f;",
            at = @At("RETURN"), // Injete no RETURN para obter o valor retornado
            cancellable = true)
    private CallbackInfoReturnable<Vector4f> barium$optimizeFogReturn(CallbackInfoReturnable<Vector4f> cir, Camera camera, int viewDistance, RenderTickCounter tickCounter, float skyDarkness, ClientWorld world, boolean thick) { // Parâmetros na ordem correta e com tipos corrigidos
        
        // Verifica se a otimização geral de névoa está ativada
        if (!BariumConfig.C.ENABLE_FOG_OPTIMIZATION) {
            return cir; // Retorna o valor original se a otimização geral estiver desativada
        }

        // Se a névoa deve ser completamente desativada
        if (BariumConfig.C.DISABLE_FOG) {
            // Retorna um Vector4f que efetivamente desativa a névoa.
            // Assumindo que X e Y representam start/end, definimos para 0.
            return cir.setReturnValue(new Vector4f(0.0f, 0.0f, 0.0f, 0.0f));
        }

        // Se a otimização de distância está ativa e a névoa não está desativada completamente:
        Vector4f originalFogValues = cir.getReturnValue();
        if (originalFogValues == null) {
            return cir; // Segurança: se o valor retornado for nulo, não faz nada.
        }

        // Obtém a render distance do jogo em blocos.
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.options == null) {
             return cir; // Segurança: se algo não estiver inicializado, retorna o original.
        }
        int playerRenderDistanceInChunks = client.options.getViewDistance().getValue();
        float baseRenderDistanceInBlocks = (float)playerRenderDistanceInChunks * 16.0f;

        // Calcula o novo valor de start da névoa.
        // FOG_START_PERCENTAGE é em porcentagem (0-100).
        float newFogStart = baseRenderDistanceInBlocks * (BariumConfig.C.FOG_START_PERCENTAGE / 100.0f);

        // Modifica o Vector4f retornado. Assumimos que x é start, y é end.
        // Se for necessário modificar o end, você pode ajustar originalFogEnd.
        Vector4f modifiedFogValues = new Vector4f(newFogStart, originalFogValues.y, originalFogValues.z, originalFogValues.w);
        
        return cir.setReturnValue(modifiedFogValues);
    }
}