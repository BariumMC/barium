package com.barium.client.mixin;

import com.barium.client.optimization.TransparentBlockOptimizer;
import net.minecraft.block.AbstractBlock; // Importar AbstractBlock
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.LeavesBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction; // Adicionado para shouldDrawSide
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin para BlockState para otimizar blocos transparentes, especialmente folhas.
 * Intercepta o método shouldDrawSide para fazer com que Faces internas de LeavesBlock distantes
 * sejam ocultadas, simulando um comportamento opaco para otimização de renderização.
 * Compatível com Minecraft 1.21.5 e Sodium.
 */
@Mixin(AbstractBlock.AbstractBlockState.class) // Ainda miramos a classe interna BlockState
public abstract class TransparentBlockMixin {

    /**
     * Injeta no início do método `shouldDrawSide` da classe `AbstractBlock.AbstractBlockState`.
     * Este método é consultado pelos renderizadores de blocos para determinar se uma face específica
     * de um bloco deve ser desenhada ou culled (ocultada).
     *
     * Se o bloco atual for uma instância de `LeavesBlock`, e o bloco adjacente na direção `direction`
     * for também uma folha (ou outro bloco que culle faces) E o bloco atual estiver além da distância de LOD,
     * podemos forçar `shouldDrawSide` a retornar `false` para as faces internas.
     *
     * Target Method Signature (Yarn 1.21.5 for BlockState.class / AbstractBlock.AbstractBlockState):
     * shouldDrawSide(Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/Direction;)Z
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