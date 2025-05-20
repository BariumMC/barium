package com.barium.mixin;

import com.barium.optimization.RedstoneOptimizer;
import net.minecraft.block.RedstoneWireBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RedstoneWireBlock.class)
public class RedstoneWireMixin {
    
    @Inject(method = "update", at = @At("HEAD"), cancellable = true)
    private void onUpdate(World world, BlockPos pos, CallbackInfo ci) {
        // Verifica se a atualização deve ser processada imediatamente ou enfileirada
        if (!RedstoneOptimizer.shouldProcessRedstoneUpdateNow(world, pos, 0, 0)) {
            ci.cancel(); // Cancela a atualização imediata
        }
    }
    
    @Inject(method = "updateNeighbors", at = @At("HEAD"), cancellable = true)
    private void onUpdateNeighbors(World world, BlockPos pos, CallbackInfo ci) {
        // Limita a propagação desnecessária de sinais
        if (!RedstoneOptimizer.shouldPropagateSignal(world, pos, pos, 0)) {
            ci.cancel(); // Cancela a propagação
        }
    }
}
