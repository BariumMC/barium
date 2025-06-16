package com.barium.client.mixin;

import com.barium.config.BariumConfig;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.entity.player.PlayerEntity; // Importe o PlayerEntity
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HopperBlockEntity.class)
public class HopperBlockEntityMixin {

    /**
     * Injeta no método 'insert' para controlar a lógica do funil.
     * Esta versão final e segura verifica periodicamente a distância do jogador mais próximo,
     * tratando corretamente o caso em que nenhum jogador é encontrado.
     */
    @Inject(method = "insert", at = @At("HEAD"), cancellable = true)
    private void barium$cullHopperLogic(CallbackInfoReturnable<Boolean> cir) {
        HopperBlockEntity self = (HopperBlockEntity)(Object)this;
        World world = self.getWorld();

        if (world == null || !world.isClient || !BariumConfig.C.ENABLE_HOPPER_TICK_CULLING) {
            return;
        }

        // CORREÇÃO: Usamos '==' para fazer a verificação apenas 1 vez a cada 8 ticks,
        // o que é muito mais eficiente.
        if (world.getTime() % 8 == 0) {
            BlockPos pos = self.getPos();
            
            // Pega o jogador mais próximo de forma segura.
            PlayerEntity closestPlayer = world.getClosestPlayer(pos.getX(), pos.getY(), pos.getZ(), 128, false);

            // A VERIFICAÇÃO DE SEGURANÇA CRÍTICA:
            // Se não houver nenhum jogador por perto, consideramos que o funil deve ser otimizado.
            if (closestPlayer == null) {
                cir.setReturnValue(false); // Diz ao jogo que "nada foi inserido".
                return;
            }

            // Agora que sabemos que 'closestPlayer' não é nulo, podemos usá-lo com segurança.
            double distanceSq = closestPlayer.getPos().squaredDistanceTo(pos.toCenterPos());
            
            if (distanceSq > BariumConfig.C.HOPPER_TICK_CULLING_DISTANCE_SQ) {
                cir.setReturnValue(false);
            }
        }
    }
}