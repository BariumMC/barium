// --- Crie este novo arquivo: src/client/java/com/barium/client/mixin/AtmosphericFogEnvironmentMixin.java ---
package com.barium.client.mixin;

import com.barium.config.BariumConfig;
import com.barium.client.SodiumExtraClientModWrapper; // Precisamos de uma forma de acessar as opções do Sodium Extra ou as nossas próprias configurações.
                                                    // Vamos usar as nossas configurações do Barium.
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera; // Pode ser necessário para obter a posição da câmera
import net.minecraft.client.render.RenderTickCounter; // Para o tickCounter
import net.minecraft.client.render.fog.FogData;
import net.minecraft.client.render.fog.FogRenderer; // Para acessar FogType se necessário
import net.minecraft.client.render.WorldRenderer; // Para acessar o Fov
import net.minecraft.client.world.ClientWorld; // Import corrgido
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Mixamos a classe que parece gerenciar a neblina atmosférica,
// baseando-nos no exemplo do Sodium Extra.
@Mixin(net.minecraft.client.render.fog.AtmosphericFogEnvironment.class) // Classe alvo correta
public class AtmosphericFogEnvironmentMixin {

    // Precisamos do `viewDistance` para calcular a névoa.
    // O `viewDistance` é passado como `int viewDistance` no método `applyFog`
    // que retornava Vector4f. No entanto, aqui em `setupFog`, o `viewDistance`
    // pode vir de outro lugar ou ser calculado.
    // Vamos tentar obter a render distance global do `MinecraftClient.options.getViewDistance().getValue()`.

    /**
     * Injete no final do método setupFog para modificar os dados da névoa.
     * O método alvo é `setupFog(FogData fogData, Entity entity, BlockPos blockPos, ClientLevel level, float viewDistance, DeltaTracker deltaTracker)`.
     * Precisamos verificar a assinatura exata no Yarn 1.21.6.
     *
     * ATENÇÃO: Se o método `setupFog` em `AtmosphericFogEnvironment` não existir ou tiver
     * uma assinatura diferente, este Mixin falhará. Precisamos confirmar a assinatura exata.
     *
     * Assinatura do método alvo (suposta, baseada no exemplo e no erro anterior):
     * setupFog(Lnet/minecraft/client/render/fog/FogData;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/core/BlockPos;Lnet/minecraft/client/world/ClientWorld;FLnet/minecraft/client/DeltaTracker;)V
     */
    @Inject(method = "setupFog(Lnet/minecraft/client/render/fog/FogData;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/core/BlockPos;Lnet/minecraft/client/world/ClientWorld;FLnet/minecraft/client/DeltaTracker;)V",
            at = @At("TAIL"), // Injete no final do método
            cancellable = true)
    private void barium$setupFogOptimization(FogData fogData, Entity entity, BlockPos blockPos, ClientWorld level, float viewDistance, net.minecraft.client.DeltaTracker deltaTracker, CallbackInfo ci) {
        
        // Verifica se a otimização geral de névoa está ativada
        if (!BariumConfig.C.ENABLE_FOG_OPTIMIZATION) {
            return;
        }

        // Se a névoa deve ser completamente desativada
        if (BariumConfig.C.DISABLE_FOG) {
            // Para desativar completamente, definimos as distâncias de início e fim para um valor muito grande.
            // Isso pode ser feito definindo os valores no FogData diretamente.
            // Assumimos que FogData tem campos modificáveis para start/end.
            fogData.setDensity(Float.MAX_VALUE); // Tenta desativar a névoa
            // Não há um método direto para 'desativar' a neblina, então estamos ajustando a densidade.
            // Se não funcionar, pode ser que precisaríamos cancelar o método original.
            return;
        }

        // Se a otimização de distância está ativa e a névoa não está desativada completamente:
        // Modifica a distância de início e fim da névoa.

        // Obtém a render distance do jogo em blocos.
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.options == null) {
             return; // Segurança: se algo não estiver inicializado, retorna.
        }
        // A viewDistance passada no método é a render distance atual em chunks.
        // Precisamos converter para blocos (viewDistance * 16).
        // A config FOG_START_PERCENTAGE é em porcentagem (0-100).
        
        // Calcula o novo valor de start da névoa.
        float newFogStart = (float)viewDistance * 16.0f * (BariumConfig.C.FOG_START_PERCENTAGE / 100.0f);

        // A documentação do Sodium Extra sugere que 'environmentalStart' e 'environmentalEnd'
        // em FogData são os campos a serem modificados.
        fogData.environmentalStart = newFogStart;
        
        // Para o 'end', podemos manter o valor original, ou ajustá-lo também.
        // O exemplo do Sodium Extra usa `(fogDistance + 1) * 16` para o end.
        // Vamos usar essa lógica.
        float newFogEnd = (float)(viewDistance + 1) * 16.0f;
        fogData.environmentalEnd = newFogEnd;
    }
}