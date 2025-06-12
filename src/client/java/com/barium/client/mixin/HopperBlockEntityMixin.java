package com.barium.client.mixin;

import com.barium.config.BariumConfig;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HopperBlockEntity.class)
public class HopperBlockEntityMixin {

    @Inject(
        method = "tick(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private static void barium$cullHopperTicks(World world, BlockPos pos, net.minecraft.block.BlockState state, CallbackInfo ci) {
        if (world == null || !world.isClient || !BariumConfig.C.ENABLE_HOPPER_TICK_CULLING) {
            return;
        }

        // Se o funil não estiver ativo (recebendo redstone), ele já é otimizado. Não mexa.
        if (!state.get(net.minecraft.block.HopperBlock.ENABLED)) {
            return;
        }
        
        // Atualiza a cada 8 ticks (1.25x por segundo em vez de 20x) se estiver longe.
        if (world.getTime() % 8 != 0) {
            Vec3d playerPos = world.getClosestPlayer(pos.getX(), pos.getY(), pos.getZ(), 128, false)
                                  .getPos(); // Usamos o jogador mais próximo para o culling.
            double distanceSq = playerPos.squaredDistanceTo(pos.toCenterPos());
            
            if (distanceSq > BariumConfig.C.HOPPER_TICK_CULLING_DISTANCE_SQ) {
                ci.cancel();
            }
        }
    }
}