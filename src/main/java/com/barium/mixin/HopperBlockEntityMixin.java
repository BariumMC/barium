package com.barium.mixin;

import com.barium.optimization.BlockTickOptimizer;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HopperBlockEntity.class)
public class HopperBlockEntityMixin {
    
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private static void onTick(World world, BlockPos pos, BlockState state, HopperBlockEntity blockEntity, CallbackInfo ci) {
        if (!BlockTickOptimizer.shouldTickHopper(blockEntity, pos)) {
            ci.cancel(); // cancela o tick
        }
    }
}
