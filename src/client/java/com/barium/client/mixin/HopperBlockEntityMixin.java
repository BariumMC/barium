package com.barium.client.mixin;

import com.barium.config.BariumConfig;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HopperBlockEntity.class)
public class HopperBlockEntityMixin {

    // Alvo: O método estático 'tick' que agora é padrão para BlockEntities.
    @Inject(
        method = "tick(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private static void barium$cullHopperClientTick(World world, BlockPos pos, BlockState state, CallbackInfo ci) {
        // A otimização só deve rodar no cliente.
        if (!world.isClient) {
            return;
        }

        if (!BariumConfig.C.ENABLE_HOPPER_TICK_CULLING) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) {
            return;
        }

        double distanceSq = client.player.getPos().squaredDistanceTo(pos.toCenterPos());
        if (distanceSq > BariumConfig.C.HOPPER_TICK_CULLING_DISTANCE_SQ) {
            ci.cancel();
        }
    }
}