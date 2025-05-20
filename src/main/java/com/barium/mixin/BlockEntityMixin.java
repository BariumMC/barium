package com.barium.mixin;

import com.barium.optimization.BlockTickOptimizer;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.level;
import net.minecraft.block.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HopperBlockEntity.class)
public abstract class BlockEntityMixin {

    // Alvejando o método estático serverTick.
    // A assinatura completa inclui os tipos dos parâmetros.
    // Lembre-se de usar os nomes de classe completos e corretos para seus mapeamentos.
    @Inject(
        method = "serverTick(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/entity/HopperBlockEntity;)V",
        at = @At("HEAD"),
        cancellable = true
        // 'require = 0' pode ser adicionado aqui também se necessário.
    )
    private static void onHopperServerTick(Level level, BlockPos pos, BlockState state, HopperBlockEntity hopperEntity, CallbackInfo ci) {
        if (!BlockTickOptimizer.shouldTickHopper(hopperEntity, pos)) {
            ci.cancel();
        }
    }
}
