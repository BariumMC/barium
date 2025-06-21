// --- Crie ou substitua o conteúdo em: src/client/java/com/barium/client/mixin/FogRendererMixin.java ---
package com.barium.client.mixin;

import com.barium.config.BariumConfig;
// --- Imports Corrigidos ---
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.fog.FogRenderer;
// Import ClientWorld do local correto
import net.minecraft.client.world.ClientWorld; 
// Import Vector4f de org.joml
import org.joml.Vector4f; 
import net.minecraft.util.math.BlockPos; // Pode precisar para @Shadow, se reativado
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FogRenderer.class)
public abstract class FogRendererMixin {

    // --- Campos @Shadow ---
    // Estes campos podem precisar ser resolvidos se FogDensityFunction existir em outro lugar,
    // ou removidos se não forem essenciais para a otimização.
    // Por enquanto, vamos mantê-los comentados pois os tipos não foram encontrados.
    // @Shadow private FogDensityFunction fogFunction; // Erro: cannot find symbol
    // @Shadow private BlockPos fogPos; // Pode ser que BlockPos ainda exista em net.minecraft.util.math

    /**
     * Intercepta o retorno do método applyFog para modificar os parâmetros de névoa.
     * Assinatura corrigida com base na documentação Yarn 'named':
     * applyFog(Lnet/minecraft/client/render/Camera;ILnet/minecraft/client/render/RenderTickCounter;FLnet/minecraft/client/world/ClientWorld;Z)Lnet/minecraft/util/math/Vector4f;
     *
     * Parâmetros (em ordem): Camera, int, RenderTickCounter, float, ClientWorld, boolean.
     * Retorno: Vector4f.
     *
     * ATENÇÃO: Esta abordagem é especulativa pois a API de neblina mudou.
     * Supomos que Vector4f.x é fogStart e Vector4f.y é fogEnd.
     *
     * Os campos @Shadow para fogFunction e fogPos foram comentados pois os tipos não foram encontrados.
     */
    @Inject(method = "applyFog(Lnet/minecraft/client/render/Camera;ILnet/minecraft/client/render/RenderTickCounter;FLnet/minecraft/client/world/ClientWorld;Z)Lnet/minecraft/util/math/Vector4f;",
            at = @At("RETURN"), // Injete no RETURN para obter o valor retornado e modificá-lo
            cancellable = true)
    private CallbackInfoReturnable<Vector4f> barium$optimizeFogReturn(CallbackInfoReturnable<Vector4f> cir, Camera camera, int viewDistance, RenderTickCounter tickCounter, float skyDarkness, ClientWorld world, boolean thick) {
        
        // Verifica se a otimização geral de névoa está ativada
        if (!BariumConfig.C.ENABLE_FOG_OPTIMIZATION) {
            return cir; // Retorna o valor original se a otimização geral estiver desativada
        }

        // Se a névoa deve ser completamente desativada
        if (BariumConfig.C.DISABLE_FOG) {
            // Retorna um Vector4f que tenta desativar a névoa.
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
        Vector4f modifiedFogValues = new Vector4f(newFogStart, originalFogValues.y, originalFogValues.z, originalFogValues.w);
        
        return cir.setReturnValue(modifiedFogValues);
    }
}