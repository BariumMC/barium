// --- Substitua o conteúdo em: src/client/java/com/barium/client/mixin/FogRendererMixin.java ---
package com.barium.client.mixin;

import com.barium.config.BariumConfig;
// --- Imports Necessários ---
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.fog.FogRenderer;
import net.minecraft.client.world.ClientWorld; // Import corrgido
import org.joml.Vector4f; // Import JOML
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
// --- Imports que podem ser necessários, mas estamos com problemas ---
// import net.minecraft.client.render.fog.FogDensityFunction; // Erro: cannot find symbol
// import net.minecraft.client.render.fog.FogRenderer; // Já importado
// import net.minecraft.client.MinecraftClient; // Usado nas configs

@Mixin(FogRenderer.class)
public abstract class FogRendererMixin {

    // --- Campos @Shadow ---
    // Removidos temporariamente devido a erros de 'cannot find symbol' para seus tipos.
    // @Shadow private FogDensityFunction fogFunction;
    // @Shadow private BlockPos fogPos;

    /**
     * Tenta otimizar a névoa modificando o valor retornado por applyFog.
     * O método alvo é applyFog(Camera, int, boolean, RenderTickCounter, float, ClientWorld)
     * que retorna Vector4f.
     *
     * Assinatura JVM esperada:
     * applyFog(Lnet/minecraft/client/render/Camera;ILnet/minecraft/client/render/RenderTickCounter;FLnet/minecraft/client/world/ClientWorld;Z)Lnet/minecraft/util/math/Vector4f;
     *
     * Lógica adaptada do Sodium Extra:
     * - Se DISABLE_FOG: Retorna Vector4f (0,0,0,0) para tentar desativar.
     * - Se ENABLE_FOG_OPTIMIZATION: Calcula newFogStart baseado em FOG_START_PERCENTAGE e viewDistance.
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
            // Assumimos que X e Y representam start/end, definimos para 0.
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
        // A viewDistance passada no método é em chunks. Convertemos para blocos.
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