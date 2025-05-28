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

/**
 * Mixin para HopperBlockEntity para otimizar o ticking específico de hoppers.
 * Baseado nos mappings Yarn 1.21.5+build.1
 */
@Mixin(HopperBlockEntity.class)
public abstract class HopperBlockEntityMixin {

    /**
     * Injeta no início do método estático serverTick, que é chamado pelo BlockEntityTicker.
     * Verifica se o tick do hopper deve ser pulado com base nas otimizações.
     *
     * Target Method Signature (Yarn 1.21.5): Lnet/minecraft/block/entity/HopperBlockEntity;serverTick(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Lnet/minecraft/block/entity/HopperBlockEntity;)V
     */
    @Inject(
        // O método alvo é estático, então usamos o descritor completo
        method = "serverTick(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Lnet/minecraft/block/entity/HopperBlockEntity;)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private static void barium$onServerTick(World world, BlockPos pos, BlockState state, HopperBlockEntity blockEntity, CallbackInfo ci) {
        // Verifica se o tick deve ser pulado pelo otimizador
        if (BlockTickOptimizer.shouldSkipHopperTick(blockEntity, world)) {
            // Cancela a execução do método serverTick original
            ci.cancel();
        }
    }
    
    /**
     * Poderíamos adicionar um mixin semelhante para clientTick se houver otimizações relevantes
     * para o lado do cliente (ex: animações), mas as otimizações atuais são focadas na lógica do servidor.
     */
}
