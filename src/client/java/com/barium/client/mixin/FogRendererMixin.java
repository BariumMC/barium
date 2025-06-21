// --- Substitua ou crie este arquivo: src/client/java/com/barium/client/mixin/FogRendererMixin.java ---
package com.barium.client.mixin;

import com.barium.config.BariumConfig;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderTickCounter;
// Import necessário para FogType
import net.minecraft.client.render.fog.FogRenderer;
// Import necessário para ClientWorld
import net.minecraft.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vector4f; // Import necessário para Vector4f
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FogRenderer.class)
public abstract class FogRendererMixin {

    @Shadow private FogDensityFunction fogFunction;
    @Shadow private BlockPos fogPos;

    /**
     * Otimiza a névoa interceptando o retorno do método applyFog e modificando os valores.
     * O método alvo é applyFog(Camera, int, boolean, RenderTickCounter, float, ClientWorld) que retorna Vector4f.
     * Assumimos que os componentes X e Y do Vector4f representam fogStart e fogEnd, respectivamente.
     *
     * Assinatura JVM esperada:
     * applyFog(Lnet/minecraft/client/render/Camera;ILnet/minecraft/client/render/RenderTickCounter;FLnet/minecraft/client/world/ClientWorld;Z)Lnet/minecraft/util/math/Vector4f;
     *
     * ATENÇÃO: Esta lógica é especulativa devido às mudanças na API de neblina.
     * Os índices dos componentes X e Y do Vector4f podem precisar de ajuste.
     */
    @Inject(method = "applyFog(Lnet/minecraft/client/render/Camera;ILnet/minecraft/client/render/RenderTickCounter;FLnet/minecraft/client/world/ClientWorld;Z)Lnet/minecraft/util/math/Vector4f;",
            at = @At("RETURN"), // Injete no RETURN para obter o valor retornado
            cancellable = true)
    private CallbackInfoReturnable<Vector4f> barium$optimizeFogReturn(CallbackInfoReturnable<Vector4f> cir, Camera camera, int viewDistance, RenderTickCounter tickCounter, float skyDarkness, ClientWorld world, boolean thick) {
        
        // Verifica se a otimização geral de partículas está ativada
        if (!BariumConfig.C.ENABLE_PARTICLE_OPTIMIZATION) { // Usando a flag geral de otimização de partículas por enquanto
            return cir; 
        }
        
        // Se a opção específica de desativar névoa estiver ligada
        if (BariumConfig.C.DISABLE_FOG) {
            // Retorna um Vector4f que efetivamente desativa a névoa.
            // Um Vector4f com todos os valores em 0,0,0,0 pode funcionar para desativar.
            // O retorno exato pode depender de como o Minecraft usa o Vector4f para neblina.
            return cir.setReturnValue(new Vector4f(0.0f, 0.0f, 0.0f, 0.0f));
        }

        // Se a otimização de distância para névoa está ativa:
        // Modifica a distância de início da névoa com base na configuração.
        // O valor do config é em porcentagem (0-100).
        // Precisamos aplicá-lo a uma distância base. A melhor base é a render distance do jogo.
        // A 'viewDistance' passada no método é em chunks. Vamos usar isso como base.

        Vector4f originalFogValues = cir.getReturnValue();
        if (originalFogValues == null) {
            return cir; // Segurança: se o valor retornado for nulo, não faz nada.
        }

        // Pegamos os valores originais
        float originalFogStart = originalFogValues.x;
        float originalFogEnd = originalFogValues.y; // Assumindo que Y é fogEnd

        // Calculamos o novo valor de start da névoa.
        // A config FOG_START_PERCENTAGE é em porcentagem (0-100).
        // A viewDistance é em chunks. Precisamos de uma distância em blocos.
        // A render distance do jogo em blocos é viewDistance * 16.
        // O novo start é a porcentagem aplicada a essa distância base.
        
        // Nota: A 'viewDistance' passada pode ser a render distance que o jogo está usando ATUALMENTE,
        // que pode ser menor que a configurada em Options. Se for o caso, nosso cálculo será mais preciso.
        
        float newFogStart = (float)viewDistance * 16.0f * (BariumConfig.C.FOG_START_PERCENTAGE / 100.0f);

        // Definimos os novos valores. Assumimos que Y é fogEnd e não o modificamos aqui.
        // Se for necessário modificar o end, pode-se ajustar 'originalFogEnd'.
        Vector4f modifiedFogValues = new Vector4f(newFogStart, originalFogEnd, originalFogValues.z, originalFogValues.w);
        
        return cir.setReturnValue(modifiedFogValues);
    }
}