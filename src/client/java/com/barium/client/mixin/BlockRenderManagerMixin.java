// --- Substitua o conteúdo em: src/client/java/com/barium/client/mixin/BlockRenderManagerMixin.java ---
package com.barium.client.mixin;

import com.barium.config.BariumConfig;
// import net.minecraft.client.render.block.BlockModelPart; // REMOVIDO: Classe não encontrada
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List; // Ainda necessário para List genérico, mas não para BlockModelPart
// import java.util.Random; // REMOVIDO: Parâmetro não usado
import java.util.concurrent.ThreadLocalRandom;

@Mixin(BlockRenderManager.class)
public class BlockRenderManagerMixin {

    /**
     * Corrigido: A assinatura do método renderBlock foi atualizada para remover o parâmetro List<BlockModelPart> parts,
     * pois a classe BlockModelPart foi removida ou movida.
     */
    @Inject(
        // Assinatura corrigida: o último parâmetro List<BlockModelPart> parts foi removido.
        // Verifique a assinatura exata do Yarn 1.21.6 se houver mais problemas.
        method = "renderBlock(Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/world/BlockRenderView;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;Z)V",
        at = @At("HEAD"),
        cancellable = true
    )
    // O método Java precisa corresponder à nova assinatura, sem List<BlockModelPart> parts
    private void barium$cullDenseFoliage(BlockState state, BlockPos pos, BlockRenderView world, MatrixStack matrices, VertexConsumer vertexConsumer, boolean cull, CallbackInfo ci) { // Parâmetro List<BlockModelPart> parts removido
        if (!BariumConfig.C.ENABLE_DENSE_FOLIAGE_CULLING || BariumConfig.C.DENSE_FOLIAGE_CULLING_LEVEL <= 0) {
            return;
        }

        // A lógica de culling de folhagem densa ainda usa a BlockState, então ela deve funcionar.
        if (isDenseFoliage(state)) {
            if (ThreadLocalRandom.current().nextInt(4) < BariumConfig.C.DENSE_FOLIAGE_CULLING_LEVEL) {
                ci.cancel(); // Pula a renderização deste bloco de grama/arbusto
            }
        }
    }

    private boolean isDenseFoliage(BlockState state) {
        return state.isOf(Blocks.SHORT_GRASS) ||
               state.isOf(Blocks.FERN) ||
               state.isOf(Blocks.TALL_GRASS) ||
               state.isOf(Blocks.LARGE_FERN) ||
               state.isOf(Blocks.DEAD_BUSH) ||
               state.isOf(Blocks.VINE);
    }
}