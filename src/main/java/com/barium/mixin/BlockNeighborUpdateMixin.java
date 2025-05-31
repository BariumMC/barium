package com.barium.mixin;

import com.barium.optimization.RedstoneOptimizer;
import net.minecraft.block.Block; // Importe a classe Block
import net.minecraft.block.BlockState;
import net.minecraft.block.RedstoneWireBlock; // Mantenha este import para a verificação de tipo
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin para Block para otimizar a propagação de sinais de redstone,
 * aplicando a lógica apenas a RedstoneWireBlock.
 * Baseado nos mappings Yarn 1.21.5+build.1
 */
@Mixin(Block.class) // ALVO: Block.class
public abstract class BlockNeighborUpdateMixin { // Nome da classe alterado

    // O método neighborUpdate existe em Block e é herdado/implementado por RedstoneWireBlock
    @Inject(
        method = "neighborUpdater",
        at = @At("HEAD"),
        cancellable = true
    )
    private void barium$onNeighborUpdate(BlockState state, World world, BlockPos pos, net.minecraft.block.Block sourceBlock, BlockPos sourcePos, boolean notify, CallbackInfo ci) {
        // CORREÇÃO: Cast 'this' para Block para que o instanceof funcione corretamente
        if (!( (Block)(Object)this instanceof RedstoneWireBlock) ) {
            return; // Sai se não for um RedstoneWireBlock
        }

        if (RedstoneOptimizer.queueRedstoneUpdate(world, pos, sourcePos)) {
             ci.cancel();
        }
    }

    // TODO: Encontrar o método correto em RedstoneWireBlock (Yarn 1.21.5) para injetar
    //       a lógica de `limitRedstonePropagation` e `queueRedstoneUpdate` de forma eficaz.
    //       Pode ser necessário injetar em `update` ou `updateNeighbors`.
    //       Um bom candidato pode ser 'updateTarget' ou 'getReceivedPower' em RedstoneWireBlock.
}