// --- Substitua o conteúdo em: src/client/java/com/barium/client/mixin/BlockRenderManagerMixin.java ---
package com.barium.client.mixin;

import com.barium.config.BariumConfig;
// REMOVIDO: import net.minecraft.client.render.block.BlockModelPart; // Classe não encontrada
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.block.BlockRenderManager;
// IMPORTADO: net.minecraft.util.math.BlockPos; e outros já devem estar ok
import net.minecraft.util.math.BlockPos;
import net.minecraft.client.util.math.MatrixStack; // Necessário para MatrixStack
import net.minecraft.world.BlockRenderView; // Necessário para BlockRenderView
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
     * Corrigido: A assinatura do método renderBlock foi atualizada para usar os nomes ofuscados do Yarn
     * para os tipos de parâmetros e para remover o parâmetro List<BlockModelPart> parts,
     * assumindo que ele foi removido ou alterado para List<?> ou simplesmente List.
     *
     * A assinatura na anotação 'method' foi ajustada para:
     * renderBlock(Lnet/minecraft/block/BlockState;Lnet/minecraft/class_2338;Lnet/minecraft/class_1920;Lnet/minecraft/class_4587;Lnet/minecraft/class_4588;ZLjava/util/List;)V
     */
    @Inject(
        // Assinatura corrigida com nomes ofuscados do Yarn e removendo BlockModelPart.
        method = "renderBlock(Lnet/minecraft/block/BlockState;Lnet/minecraft/class_2338;Lnet/minecraft/class_1920;Lnet/minecraft/class_4587;Lnet/minecraft/class_4588;ZLjava/util/List;)V",
        at = @At("HEAD"),
        cancellable = true
    )
    // O método Java precisa corresponder à nova assinatura, recebendo os tipos corretos
    private void barium$cullDenseFoliage(BlockState state, BlockPos pos, BlockRenderView world, MatrixStack matrices, VertexConsumer vertexConsumer, boolean cull, List<?> parts, CallbackInfo ci) { // Parâmetro List<?> parts para acomodar a possível mudança de tipo genérico
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