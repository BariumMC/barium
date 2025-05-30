package com.barium.mixin;

import com.barium.optimization.RedstoneOptimizer;
import net.minecraft.block.BlockState;
import net.minecraft.block.RedstoneWireBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RedstoneWireBlock.class)
public abstract class RedstoneWireMixin {

    // O método neighborUpdate existe em Block e é herdado/implementado por RedstoneWireBlock
    @Inject(
        method = "neighborUpdate(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/Block;Lnet/minecraft/util/math/BlockPos;Z)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void barium$onNeighborUpdate(BlockState state, World world, BlockPos pos, net.minecraft.block.Block sourceBlock, BlockPos sourcePos, boolean notify, CallbackInfo ci) {
        if (RedstoneOptimizer.queueRedstoneUpdate(world, pos, sourcePos)) {
             ci.cancel();
        }
    }

    // TODO: Encontrar o método correto em RedstoneWireBlock (Yarn 1.21.5) para injetar
    //       a lógica de `limitRedstonePropagation` e `queueRedstoneUpdate` de forma eficaz.
    //       Pode ser necessário injetar em `update` ou `updateNeighbors`.
    //       Um bom candidato pode ser 'updateTarget' ou 'getReceivedPower' em RedstoneWireBlock.
}