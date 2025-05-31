package com.barium.client.mixin;

import com.barium.client.optimization.TransparentBlockOptimizer;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.LeavesBlock;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.block.BlockModelRenderer;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.model.ModelLoader; // Apenas para assinatura
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random; // Apenas para assinatura
import net.minecraft.world.BlockRenderView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * Mixin para BlockModelRenderer para otimizar a renderização de blocos transparentes, especialmente folhas.
 * Intercepta a renderização de quads para folhas distantes, permitindo o culling de faces internas.
 * Compatível com Minecraft 1.21.5 e Sodium.
 */
@Mixin(BlockModelRenderer.class)
public abstract class TransparentBlockModelRendererMixin {

    /**
     * Injeta no início do método `renderQuads` da classe `BlockModelRenderer`.
     * Este método é responsável por renderizar os quads (faces) de um bloco.
     *
     * Se o bloco que está sendo renderizado for uma `LeavesBlock` e estiver além da distância de LOD,
     * e a face atual for uma face interna (voltada para outra folha), nós cancelamos a execução
     * do método, impedindo a renderização dessa face.
     *
     * Target Method Signature (Yarn 1.21.5 for BlockModelRenderer.class):
     * renderQuads(Lnet/minecraft/world/BlockRenderView;Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/client/render/VertexConsumer;Ljava/util/List;Lnet/minecraft/world/BlockRenderView;Lnet/minecraft/util/math/Direction;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/LightmapTextureManager;I)V
     *
     * Note: A assinatura do método `renderQuads` é bem longa. O Mixin precisa da assinatura exata.
     * Certifique-se de que o Loom está puxando os mappings corretos para 1.21.5.
     */
    @Inject(
        method = "renderQuads(Lnet/minecraft/world/BlockRenderView;Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/client/render/VertexConsumer;Ljava/util/List;Lnet/minecraft/world/BlockRenderView;Lnet/minecraft/util/math/Direction;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/LightmapTextureManager;I)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void barium$onRenderQuads(
        BlockRenderView world,                 // arg 0: world
        BlockState state,                     // arg 1: state
        BlockPos pos,                         // arg 2: pos
        VertexConsumer consumer,              // arg 3: buffer
        List<BakedQuad> quads,                // arg 4: quads
        BlockRenderView world2,               // arg 5: worldIn (redundant but in signature)
        Direction direction,                  // arg 6: face (direction for the quads)
        MatrixStack matrices,                 // arg 7: matrices
        Object lightmapManager,               // arg 8: lightmapTextureManager (Object for simplicity if not used)
        int overlay,                          // arg 9: overlay
        CallbackInfo ci
    ) {
        // Apenas aplica a otimização se o bloco atual for uma folha
        if (state.getBlock() instanceof LeavesBlock) {
            // Verifica se as folhas nesta posição devem ser simplificadas com base na distância
            if (TransparentBlockOptimizer.shouldSimplifyLeaves(world, pos)) {
                // Se a face é uma face interna (ou seja, não é virada para o ar, mas para outro bloco)
                // e o bloco vizinho na mesma direção também é uma folha, podemos culll.
                // Esta é a mesma lógica de culling que blocos opacos usam para suas faces internas.
                BlockState neighborState = world.getBlockState(pos.offset(direction));

                // Se o bloco vizinho também for uma folha, oculte esta face.
                if (neighborState.getBlock() instanceof LeavesBlock) {
                    ci.cancel(); // Cancela a renderização desta face (quad).
                    return;      // Sai do método Mixin.
                }
                // Se o vizinho não for uma folha (ex: ar, tronco, etc.), a face externa ainda deve ser renderizada.
            }
        }
        // Se não for LeavesBlock, ou não estiver distante, ou não for uma face interna,
        // permite que o método original `renderQuads` seja executado.
    }
}