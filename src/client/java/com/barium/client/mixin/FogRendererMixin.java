// --- Substitua o conteúdo em: src/client/java/com/barium/client/mixin/FogRendererMixin.java ---
package com.barium.client.mixin;

import com.barium.config.BariumConfig;
// --- Imports Corrigidos e Ajustados ---
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderTickCounter;
// FogRenderer já é importado pelo @Mixin
import net.minecraft.client.render.fog.FogRenderer;
// Import ClientWorld de forma padrão, se não encontrar, é um problema de mapeamento
import net.minecraft.world.ClientWorld;
// Import Vector4f de org.joml
import org.joml.Vector4f;
// --- Outros imports necessários ---
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FogRenderer.class)
public abstract class FogRendererMixin {

    // --- Campos Shadow ---
    // Removidos os campos @Shadow que referenciam tipos não encontrados.
    // Se FogDensityFunction for necessário e pudermos encontrar seu tipo, podemos reativá-los.
    // @Shadow private FogDensityFunction fogFunction; // REMOVIDO
    // @Shadow private BlockPos fogPos; // REMOVIDO

    /**
     * Tenta otimizar a névoa interceptando o retorno do método applyFog.
     * O método alvo é applyFog(Camera, int, boolean, RenderTickCounter, float, ClientWorld)
     * que retorna Vector4f.
     *
     * Assumindo a seguinte assinatura baseada na documentação e no erro anterior:
     * applyFog(Lnet/minecraft/client/render/Camera;ILnet/minecraft/client/render/RenderTickCounter;FLnet/minecraft/client/world/ClientWorld;Z)Lnet/minecraft/util/math/Vector4f;
     *
     * Este Mixin tentará modificar a distância da névoa.
     */
    @Inject(method = "applyFog(Lnet/minecraft/client/render/Camera;ILnet/minecraft/client/render/RenderTickCounter;FLnet/minecraft/client/world/ClientWorld;Z)Lnet/minecraft/util/math/Vector4f;",
            at = @At("RETURN"), // Injete no RETURN para obter o valor retornado
            cancellable = true)
    private CallbackInfoReturnable<Vector4f> barium$optimizeFogReturn(CallbackInfoReturnable<Vector4f> cir, Camera camera, int viewDistance, RenderTickCounter tickCounter, float skyDarkness, ClientWorld world, boolean thick) {
        
        // Verifica se a otimização geral de névoa está ativada
        if (!BariumConfig.C.ENABLE_FOG_OPTIMIZATION) {
            return cir; // Retorna o valor original se a otimização geral estiver desativada
        }

        // Se a névoa deve ser completamente desativada
        if (BariumConfig.C.DISABLE_FOG) {
            // Retorna um Vector4f que efetivamente desativa a névoa.
            // Assumindo que X=start, Y=end, definimos para 0.
            return cir.setReturnValue(new Vector4f(0.0f, 0.0f, 0.0f, 0.0f));
        }

        // Se a otimização de distância está ativa e a névoa não está desativada completamente:
        // Modifica a distância de início da névoa com base na configuração.
        
        Vector4f originalFogValues = cir.getReturnValue();
        if (originalFogValues == null) {
            return cir; // Segurança: se o valor retornado for nulo, não faz nada.
        }

        // Obtém a render distance do jogo em blocos.
        // O parâmetro 'viewDistance' no método applyFog é em chunks.
        // Precisamos converter para blocos (viewDistance * 16) para aplicar a porcentagem.
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