// --- Substitua o conteúdo em: src/client/java/com/barium/client/mixin/BlockRenderManagerMixin.java ---
package com.barium.client.mixin;

import com.barium.config.BariumConfig;
// REMOVIDO: import net.minecraft.client.render.block.BlockModelPart; // Classe não encontrada
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos; // Mapeamento Yarn: Lnet/minecraft/class_2338;
import net.minecraft.world.BlockRenderView; // Mapeamento Yarn: Lnet/minecraft/class_1920;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List; // Necessário para List genérico
// import java.util.Random; // REMOVIDO: Parâmetro não usado
import java.util.concurrent.ThreadLocalRandom;

@Mixin(BlockRenderManager.class)
public class BlockRenderManagerMixin {

    /**
     * Corrigido: A assinatura do método renderBlock foi atualizada para usar os nomes "named" do Yarn
     * e para remover o parâmetro List<BlockModelPart> parts, pois a classe BlockModelPart foi removida/movida
     * e a assinatura esperada agora é List genérico.
     *
     * A assinatura correta com nomes "named" e List genérico é:
     * renderBlock(BlockState state, BlockPos pos, BlockRenderView world, MatrixStack matrices, VertexConsumer vertexConsumer, boolean cull, List<?> parts)
     * Representação JVM:
     * Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/world/BlockRenderView;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;ZLjava/util/List;
     */
    @Inject(
        // Assinatura corrigida para usar nomes "named" e List genérico no final.
        method = "renderBlock(Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/world/BlockRenderView;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;ZLjava/util/List;)V",
        at = @At("HEAD"),
        cancellable = true
    )
    // O método Java precisa corresponder à nova assinatura, recebendo List<?> parts
    private void barium$cullDenseFoliage(BlockState state, BlockPos pos, BlockRenderView world, MatrixStack matrices, VertexConsumer vertexConsumer, boolean cull, List<?> parts, CallbackInfo ci) { // Parâmetro List<?> parts agora está presente
        if (!BariumConfig.C.ENABLE_DENSE_FOLIAGE_CULLING || BariumConfig.C.DENSE_FOLIAGE_CULLING_LEVEL <= 0) {
            return;
        }

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