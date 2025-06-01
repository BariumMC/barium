package com.barium.client.mixin;

import com.barium.client.optimization.TransparentBlockOptimizer;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.block.BlockModelRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin para otimizar a renderização de blocos transparentes aplicando culling (ocultação)
 * para blocos distantes, com base em TransparentBlockOptimizer.ENABLE_TRANSPARENT_BLOCK_CULLING.
 * Alvo: BlockModelRenderer#render para evitar a renderização se o bloco estiver muito distante.
 */
@Mixin(BlockModelRenderer.class)
public abstract class TransparentBlockCullingMixin {

    /**
     * Injeta no início do método render de BlockModelRenderer para aplicar o culling de blocos transparentes.
     * Se o bloco deve ser culled, a renderização é cancelada.
     *
     * Target: net.minecraft.client.render.block.BlockModelRenderer#render(Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/world/BlockRenderView;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;ZJ)Z
     * A assinatura correta do método `render` em `BlockModelRenderer` para 1.21.5 (Yarn) é tipicamente
     * `render(BlockState state, BlockPos pos, BlockRenderView world, MatrixStack matrices, VertexConsumer consumer, boolean checkSides, long seed)`
     * ou uma sobrecarga similar. O parâmetro `cull` no final da sua assinatura original era `checkSides`.
     */
    @Inject(
        method = "render(Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/world/BlockRenderView;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;ZJ)Z",
        at = @At("HEAD"),
        cancellable = true
    )
    private void barium$cullTransparentBlock(
        BlockState state, BlockPos pos, BlockRenderView world, MatrixStack matrices,
        VertexConsumer consumer, boolean checkSides, long seed, CallbackInfoReturnable<Boolean> cir) { // Renomeado 'cull' para 'checkSides'

        // Garante que o 'world' é uma instância de ClientWorld para verificações de distância.
        if (!(world instanceof net.minecraft.client.world.ClientWorld clientWorld)) {
            return;
        }

        // Se o otimizador decidir que o bloco transparente deve ser culled, cancela a renderização.
        if (TransparentBlockOptimizer.shouldCullTransparentBlock(state, pos, clientWorld)) {
            cir.setReturnValue(false); // Retorna falso, indicando que o bloco não foi renderizado com sucesso.
        }
    }
}