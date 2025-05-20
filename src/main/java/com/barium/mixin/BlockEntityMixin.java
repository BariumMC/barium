package com.barium.mixin;

import com.barium.optimization.BlockTickOptimizer;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.HopperBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HopperBlockEntity.class)
public class BlockEntityMixin {

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private static void onTick(net.minecraft.world.World world, net.minecraft.block.BlockPos pos, net.minecraft.block.BlockState state, HopperBlockEntity blockEntity, CallbackInfo ci) {
        if (!BlockTickOptimizer.shouldTickHopper(blockEntity, pos)) {
            ci.cancel(); // Cancela o tick
        }
    }
}
