package com.barium.client.mixin.block_rendering;

import com.barium.client.mixin.accesor.BakedModelInvoker;
import com.barium.config.BariumConfig;
import net.minecraft.block.BlockState;
import net.minecraft.block.LeavesBlock;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.block.BlockModelRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockRenderView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Collections;
import java.util.List;

@Mixin(BlockModelRenderer.class)
public abstract class BlockModelRendererMixin {

    // Redireciona a chamada para getQuads dentro do renderSmooth
    @Redirect(method = "renderSmooth", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/model/BakedModel;getQuads(Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/Direction;Lnet/minecraft/util/math/random/Random;)Ljava/util/List;"))
    private List<BakedQuad> barium$aggressiveFaceCulling$getQuadsSmooth(BakedModel bakedModel, BlockState state, Direction face, Random random, BlockRenderView world, BakedModel model, BlockState originalState, BlockPos pos, MatrixStack matrices, VertexConsumer consumer, boolean cull, Random originalRandom, long seed, int overlay) {
        return getFilteredQuads(bakedModel, state, face, random, world, pos);
    }

    // Redireciona a chamada para getQuads dentro do renderFlat
    @Redirect(method = "renderFlat", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/model/BakedModel;getQuads(Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/Direction;Lnet/minecraft/util/math/random/Random;)Ljava/util/List;"))
    private List<BakedQuad> barium$aggressiveFaceCulling$getQuadsFlat(BakedModel bakedModel, BlockState state, Direction face, Random random, BlockRenderView world, BakedModel model, BlockState originalState, BlockPos pos, MatrixStack matrices, VertexConsumer consumer, boolean cull, Random originalRandom, long seed, int overlay) {
        return getFilteredQuads(bakedModel, state, face, random, world, pos);
    }

    // Método auxiliar para aplicar as regras de culling
    private List<BakedQuad> getFilteredQuads(BakedModel bakedModel, BlockState blockState, Direction face, Random random, BlockRenderView world, BlockPos pos) {
        // Obtém as quads originais do modelo
        List<BakedQuad> vanillaQuads = ((BakedModelInvoker) bakedModel).invokeGetQuads(blockState, face, random);

        if (vanillaQuads.isEmpty()) {
            return vanillaQuads; // Nada para otimizar se não houver quads
        }

        // --- Otimização de Culling Agressivo para Faces ---
        if (BariumConfig.get().render.aggressiveFaceCulling && face != null) { // Apenas para faces direcionais
            BlockPos neighborPos = pos.offset(face);
            BlockState neighborState = world.getBlockState(neighborPos);

            // Culling para Folhas: Se uma face de folha estiver adjacente a um bloco sólido (que a esconderia visualmente),
            // ou outra folha que deveria ocultar, não renderize.
            // O Minecraft já faz algo parecido com `isSideInvisible`, mas podemos ser mais agressivos.
            if (blockState.getBlock() instanceof LeavesBlock) {
                // Se o bloco vizinho for uma folha e invisível do nosso lado, ou se for um bloco opaco, culling.
                // Isso visa evitar a renderização de folhas "internas" em grandes massas de folhagem.
                if (neighborState.getBlock() instanceof LeavesBlock && neighborState.isSideInvisible(blockState, face.getOpposite())) {
                    return Collections.emptyList();
                }
                // Mais agressivo: se o vizinho for totalmente opaco, culling. (Pode causar glitches em modelos complexos)
                if (neighborState.isOpaqueFullCube(world, neighborPos)) {
                    return Collections.emptyList();
                }
            }
        }

        // --- Otimização de Renderização de Fluido ---
        if (BariumConfig.get().render.optimizeFluidRendering && blockState.getFluidState().isStill() && face != null) {
            // Para fluidos parados, se a face não for a superior e estiver adjacente a um fluido idêntico,
            // podemos pular a renderização para evitar overdraw.
            if (face != Direction.UP) { // Sempre renderizar a face superior dos fluidos
                BlockPos neighborPos = pos.offset(face);
                FluidState neighborFluid = world.getFluidState(neighborPos);
                if (neighborFluid.isStill() && neighborFluid.isEqualAndStill(blockState.getFluidState())) {
                    return Collections.emptyList(); // Pular a renderização de faces internas de fluidos
                }
            }
        }

        return vanillaQuads; // Retorna as quads originais se nenhuma regra de culling se aplicou
    }
}