package com.barium.client.mixin;

import com.barium.config.BariumConfig;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HopperBlockEntity.class)
public class HopperBlockEntityMixin {

    /**
     * Em vez de injetar no 'tick', injetamos no método 'insert', que é a operação principal
     * do funil. Se o funil estiver longe, cancelamos a tentativa de inserção,
     * economizando a busca por inventários e outras verificações caras.
     *
     * A CORREÇÃO CRÍTICA é especificar a assinatura completa "insert()Z" para desambiguar
     * e garantir que estamos injetando no método de instância, não no estático.
     */
    @Inject(method = "insert()Z", at = @At("HEAD"), cancellable = true)
    private void barium$cullHopperLogic(CallbackInfoReturnable<Boolean> cir) {
        HopperBlockEntity self = (HopperBlockEntity)(Object)this;
        World world = self.getWorld();

        if (world == null || !world.isClient || !BariumConfig.C.ENABLE_HOPPER_TICK_CULLING) {
            return;
        }

        // A lógica de culling só se aplica em ticks espaçados para reduzir a verificação de distância.
        if (world.getTime() % 8 == 0) { // Verifica 1 vez a cada 8 ticks
            BlockPos pos = self.getPos();
            
            // Verificação rápida: se não houver jogador por perto, pula a lógica cara.
            // A distância de 64 é um bom valor padrão.
            if (!world.isPlayerInRange(pos.getX(), pos.getY(), pos.getZ(), 64)) {
                cir.setReturnValue(false); // Diz ao jogo que "nada foi inserido"
                return;
            }

            // Verificação mais precisa se houver um jogador próximo.
            // Esta lógica está correta, mas a chamada a `getClosestPlayer` pode ser cara.
            // A verificação `isPlayerInRange` acima já filtra a maioria dos casos.
            if (world.getClosestPlayer(pos.getX(), pos.getY(), pos.getZ(), 128, false) == null) {
                // Se não há jogador mesmo em uma área maior, cancela.
                cir.setReturnValue(false);
            }
            // Se um jogador for encontrado, a lógica de inserção do funil continua normalmente.
            // Uma melhoria futura poderia ser adicionar a verificação de distância aqui, mas isso já
            // resolve o crash e fornece uma otimização significativa.
        }
    }
}