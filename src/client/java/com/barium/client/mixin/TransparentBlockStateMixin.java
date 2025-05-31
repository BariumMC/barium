package com.barium.client.mixin;

import com.barium.client.optimization.TransparentBlockOptimizer;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.LeavesBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map; // Necessário para o construtor do AbstractBlock.AbstractBlockState
import net.minecraft.state.property.Property; // Necessário para o construtor do AbstractBlock.AbstractBlockState

/**
 * Mixin para BlockState para otimizar blocos transparentes, especialmente folhas.
 * Quando as folhas estão distantes, elas podem ser tratadas como opacas para reduzir o overhead de renderização.
 * Compatível com Minecraft 1.21.5 e Sodium.
 */
@Mixin(BlockState.class)
public abstract class TransparentBlockStateMixin extends AbstractBlock.AbstractBlockState {

    // O construtor é necessário porque BlockState é um AbstractBlock.AbstractBlockState abstrato.
    protected TransparentBlockStateMixin(Block block, Map<Property<?>, Comparable<?>> properties) {
        super(block, properties);
    }

    /**
     * Injeta no início do método `isOpaqueFullCubeIn` para potencialmente fazer com que
     * `LeavesBlock` (blocos de folhas) distantes reportem como cubos cheios opacos.
     * Isso pode reduzir o overdraw e simplificar a renderização, fazendo com que o motor
     * de renderização (e o Sodium) os trate como blocos sólidos à distância.
     *
     * Target Method Signature (Yarn 1.21.5): isOpaqueFullCubeIn(Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;)Z
     */
    @Inject(
        method = "isOpaqueFullCubeIn(Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;)Z",
        at = @At("HEAD"),
        cancellable = true // Permite que o mixin cancele a execução do método original
    )
    private void barium$isOpaqueFullCubeIn(BlockView world, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        // 'this' refere-se à instância de BlockState sendo processada
        Block selfBlock = ((BlockState)(Object)this).getBlock();

        // Aplica a otimização apenas se o bloco for uma instância de LeavesBlock
        if (selfBlock instanceof LeavesBlock) {
            // Verifica com o otimizador se as folhas nesta posição devem ser simplificadas
            if (TransparentBlockOptimizer.shouldSimplifyLeaves(world, pos)) {
                // Se sim, força o retorno para 'true', tratando as folhas como opacas
                cir.setReturnValue(true);
            }
        }
        
        // TODO (Opcional): Implementar culling para outros blocos transparentes
        // Se BariumConfig.ENABLE_TRANSPARENT_BLOCK_CULLING for verdadeiro e
        // TransparentBlockOptimizer.shouldCullTransparentBlock(world, pos) for verdadeiro,
        // você poderia injetar em um método de renderização de bloco (não BlockState diretamente)
        // para impedir a renderização de blocos transparentes distantes.
        // Isso seria mais complexo e poderia envolver mixins em ChunkRenderer/BuiltChunk.
        // Para a função isOpaqueFullCubeIn, este ponto não é o ideal para culling completo.
    }
}