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
     */
    @Inject(
        method = "render(Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/world/BlockRenderView;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;ZJ)Z",
        at = @At("HEAD"),
        cancellable = true
    )
    private void barium$cullTransparentBlock(
        BlockState state, BlockPos pos, BlockRenderView world, MatrixStack matrices,
        VertexConsumer consumer, boolean cull, long seed, CallbackInfoReturnable<Boolean> cir) {

        // Garante que o 'world' é uma instância de ClientWorld para verificações de distância.
        if (!(world instanceof net.minecraft.client.world.ClientWorld clientWorld)) {
            return;
        }

        // Se o otimizador decidir que o bloco transparente deve ser culled, cancela a renderização.
        if (TransparentBlockOptimizer.shouldCullTransparentBlock(state, pos, clientWorld)) {
            cir.setReturnValue(false); // Retorna falso, indicando que o bloco não foi renderizado com sucesso.
        }
    }

    // Nota sobre LOD para Folhas (ENABLE_LEAVES_LOD):
    // A implementação de LOD para folhas (torná-las opacas em distância) é mais complexa.
    // Ela geralmente envolve a substituição do RenderLayer para BlockState distantes (de TRANSLUCENT/CUTOUT para SOLID)
    // ou a modificação dinâmica do BakedModel para usar uma versão opaca.
    // O método `TransparentBlockOptimizer.shouldRenderLeavesAsOpaqueLOD` fornece a lógica de decisão,
    // mas a injeção em BlockModelRenderer aqui para efetivamente alterar o comportamento de renderização
    // para um RenderLayer diferente seria complexa (ex: exigiria um Wrapper VertexConsumer ou ModifyArgs
    // para mudar o 'consumer' baseado na distância).
    // Para uma implementação mais simples e direta, o culling é a abordagem proposta.
    // Se você precisar de LOD visual para folhas, seria necessário um mixin mais avançado
    // talvez em `net.minecraft.client.render.RenderLayers#get(BlockState)` (se o contexto de World/BlockPos puder ser obtido)
    // ou manipulando o VertexConsumer diretamente.
}