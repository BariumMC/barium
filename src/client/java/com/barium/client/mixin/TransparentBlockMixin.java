package com.barium.client.mixin;

import com.barium.client.optimization.TransparentBlockOptimizer;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.LeavesBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin para BlockState para otimizar blocos transparentes, especialmente folhas.
 * Intercepta o método isOpaque para fazer com que LeavesBlock distantes
 * se comportem como opacos para fins de renderização.
 * Compatível com Minecraft 1.21.5 e Sodium.
 */
@Mixin(BlockState.class) // Voltamos a mixar BlockState
public abstract class TransparentBlockMixin { // Mantemos o nome TransparentBlockMixin

    /**
     * Injeta no início do método `isOpaque` da classe `BlockState`.
     * Este método é fundamental para o sistema de renderização determinar se um bloco
     * é totalmente opaco (e pode ser renderizado no passe opaco) ou se requer transparência.
     *
     * Se o bloco atual for uma instância de `LeavesBlock` e estiver além da distância de LOD,
     * forçamos `isOpaque` a retornar `true`, fazendo com que as folhas distantes sejam tratadas
     * como blocos sólidos/opacos pelo motor de renderização (e Sodium).
     *
     * Target Method Signature (Yarn 1.21.5 for BlockState.class):
     * isOpaque(Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;)Z
     */
    @Inject(
        method = "isOpaque(Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;)Z",
        at = @At("HEAD"),
        cancellable = true
    )
    private void barium$isOpaque(BlockView world, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        // 'this' refere-se à instância de BlockState.
        Block selfBlock = ((BlockState)(Object)this).getBlock();

        // Aplicamos a otimização apenas se o bloco for uma instância de LeavesBlock
        if (selfBlock instanceof LeavesBlock) {
            // Verifica com o otimizador se as folhas nesta posição devem ser simplificadas
            if (TransparentBlockOptimizer.shouldSimplifyLeaves(world, pos)) {
                // Se sim, força o retorno para 'true', tratando as folhas como opacas.
                // Isso fará com que sejam renderizadas no passe opaco e potencialmente
                // tenham suas faces internas culled pelo Sodium.
                cir.setReturnValue(true);
            }
        }
    }
}