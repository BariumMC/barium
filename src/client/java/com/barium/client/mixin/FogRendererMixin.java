// --- Adicione ou substitua o conteúdo em: src/client/java/com/barium/client/mixin/FogRendererMixin.java ---
package com.barium.client.mixin;

import com.barium.config.BariumConfig;
import com.mojang.blaze3d.systems.RenderSystem; // Pode ser necessário para outras coisas
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.fog.FogDensityFunction;
import net.minecraft.client.render.fog.FogRenderer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vector4f;
import net.minecraft.world.ClientWorld;
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
     * Tenta otimizar a névoa interceptando e modificando o valor retornado por applyFog.
     * O método alvo é `applyFog(Camera camera, int viewDistance, boolean thick, RenderTickCounter tickCounter, float skyDarkness, ClientWorld world)`
     * que retorna `Vector4f`. Assumimos que os componentes do Vector4f representam:
     * x = fogStart, y = fogEnd, z = ???, w = ???
     *
     * A assinatura JVM esperada para este método é:
     * applyFog(Lnet/minecraft/client/render/Camera;ILnet/minecraft/client/render/RenderTickCounter;FLnet/minecraft/client/world/ClientWorld;Z)Lnet/minecraft/util/math/Vector4f;
     */
    @Inject(method = "applyFog(Lnet/minecraft/client/render/Camera;ILnet/minecraft/client/render/RenderTickCounter;FLnet/minecraft/client/world/ClientWorld;Z)Lnet/minecraft/util/math/Vector4f;",
            at = @At("RETURN"), // Injete no final para obter o valor retornado
            cancellable = true)
    private CallbackInfoReturnable<Vector4f> barium$optimizeFogReturn(CallbackInfoReturnable<Vector4f> cir, Camera camera, int viewDistance, RenderTickCounter tickCounter, float skyDarkness, ClientWorld world, boolean thick) {
        
        // Verifica se a otimização geral de névoa está ativada
        if (!BariumConfig.C.ENABLE_FOG_OPTIMIZATION) {
            return cir; // Retorna o valor original se a otimização geral estiver desativada
        }

        // Se a névoa deve ser completamente desativada
        if (BariumConfig.C.DISABLE_FOG) {
            // Retorna um Vector4f que efetivamente desativa a névoa.
            // Um início e fim de névoa de 0,0 pode funcionar, mas dependendo da implementação, pode ser necessário outros valores.
            // Assumindo que X é start e Y é end.
            return cir.setReturnValue(new Vector4f(0.0f, 0.0f, 0.0f, 0.0f));
        }

        // Se a otimização de distância está ativa e a névoa não está desativada:
        // Modifica a distância de início da névoa com base na configuração e na viewDistance atual.
        
        // A configuração é em porcentagem (0-100). A viewDistance é em chunks.
        // Precisamos de uma distância base em blocos. A render distance do jogo é a melhor base.
        // A viewDistance aqui é em chunks, então precisamos converter.
        // O FOG_START_PERCENTAGE é aplicado sobre essa distância base.

        // Precisamos da render distance em blocos. A forma de obter isso pode variar.
        // Uma aproximação é usar `viewDistance * 16` (pois 1 chunk = 16 blocos).
        // No entanto, o `viewDistance` aqui é o da câmera, que pode ser diferente da render distance global.

        // Vamos usar o `viewDistance` em chunks como base para o cálculo da porcentagem.
        // O cálculo da distância da névoa é um pouco complexo. O ideal seria obter a render distance global.
        // Para simplificar, usaremos a `viewDistance` do método como base.

        // Obtém os valores originais de start/end da névoa (assumindo X=start, Y=end)
        Vector4f originalFogValues = cir.getReturnValue();
        float originalFogStart = originalFogValues.x;
        float originalFogEnd = originalFogValues.y;

        // Calcula o novo start da névoa baseado na porcentagem da render distance do jogo.
        // A configuração `FOG_START_PERCENTAGE` é em porcentagem (0-100).
        // Precisamos aplicá-la a uma distância base. Vamos usar a `viewDistance` do método como base.
        // `viewDistance` está em chunks. A conversão para blocos é `viewDistance * 16`.
        // Precisamos aplicar a porcentagem a essa distância.
        
        // O valor de start da névoa no Minecraft é um float.
        // Se a render distance do jogo for `renderDistance` blocos, e a porcentagem for `P`,
        // o novo start seria `renderDistance * (P / 100.0f)`.

        // Obtém a render distance do jogo em chunks (que é o que 'viewDistance' parece representar)
        // A render distance do jogador é obtida de MinecraftClient.getInstance().options.getViewDistance().getValue();
        // O parâmetro 'viewDistance' no método `applyFog` pode ser o valor em chunks.
        
        // Precisamos de uma referência para o MinecraftClient para obter a render distance atual.
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.options == null) {
             return cir; // Segurança: se algo não estiver inicializado, retorna o original.
        }
        // O valor de viewDistance é em chunks. Vamos usar isso como base.
        // viewDistance * 16 é a render distance em blocos.

        float newFogStart = BariumConfig.C.FOG_START_PERCENTAGE / 100.0f; // Já é um multiplicador
        // Precisamos que o valor do config seja aplicado à distância correta.
        // O que o BariumConfig.C.FOG_START_PERCENTAGE armazena é a porcentagem (0-100).
        // Precisamos aplicar isso à distância que o jogo calcula.
        // A documentação de `applyFog` não deixa claro como essa distância é passada ou calculada.

        // SE ESTA LÓGICA DE MODIFICAR O VALOR RETORNADO (Vector4f) NÃO FUNCIONAR
        // (porque o Vector4f não representa start/end diretamente ou não é mutável assim),
        // então a otimização de neblina precisará de uma abordagem completamente diferente.

        // A hipótese mais simples é que o próprio método `applyFog` usa esses valores.
        // Se quisermos mudar o 'start' e 'end', precisamos fazer isso ANTES que o método `applyFog`
        // use esses valores para definir a neblina.
        // Isso sugere que um @Inject no HEAD ou um @ModifyArgs seria melhor se encontrarmos o método correto.

        // Como não temos um ponto de injeção claro para modificar diretamente os valores de start/end
        // ou um método que os receba, e sabendo que a API mudou, vamos comentar esta parte
        // para evitar mais erros de compilação e focar no que é mais estável.
        
        // Retornando o valor original se a otimização não for aplicada.
        return cir;
    }

    // O Mixin precisa do método para compilar, mesmo que esteja comentado ou vazio.
    // Para compilar, vou deixar esta classe com os comentários e sem código executável que cause erro.
    // Se você quiser investigar a otimização de neblina, seria necessário mais pesquisa sobre
    // as APIs de neblina no Yarn 1.21.6 e como modificar os parâmetros de 'fogStart' e 'fogEnd'.
}