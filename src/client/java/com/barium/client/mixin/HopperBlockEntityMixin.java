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
     */
    @Inject(method = "insert", at = @At("HEAD"), cancellable = true)
    private void barium$cullHopperLogic(CallbackInfoReturnable<Boolean> cir) {
        HopperBlockEntity self = (HopperBlockEntity)(Object)this;
        World world = self.getWorld();

        if (world == null || !world.isClient || !BariumConfig.C.ENABLE_HOPPER_TICK_CULLING) {
            return;
        }

        // A lógica de culling só se aplica em ticks espaçados para reduzir a verificação de distância.
        if (world.getTime() % 8 != 0) {
            BlockPos pos = self.getPos();
            
            // Verificação rápida: se não houver jogador por perto, pula a lógica cara.
            if (!world.isPlayerInRange(pos.getX(), pos.getY(), pos.getZ(), 64)) {
                cir.setReturnValue(false); // Diz ao jogo que "nada foi inserido"
                return;
            }

            // Verificação mais precisa se houver um jogador próximo
            double distanceSq = world.getClosestPlayer(pos.getX(), pos.getY(), pos.getZ(), 128, false)
                                  .getPos().squaredDistanceTo(pos.toCenterPos());
            
            if (distanceSq > BariumConfig.C.HOPPER_TICK_CULLING_DISTANCE_SQ) {
                cir.setReturnValue(false);
            }
        }
    }
}