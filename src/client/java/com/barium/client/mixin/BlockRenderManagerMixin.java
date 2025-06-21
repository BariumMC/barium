// --- Substitua o conteúdo em: src/client/java/com/barium/client/mixin/BlockRenderManagerMixin.java ---
package com.barium.client.mixin;

import com.barium.config.BariumConfig;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.block.BlockModelPart; // Import necessário para List<BlockModelPart>
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random; // Este import pode não ser mais necessário se o parâmetro for removido
import net.minecraft.world.BlockRenderView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List; // Import necessário para List
import java.util.concurrent.ThreadLocalRandom;

@Mixin(BlockRenderManager.class)
public class BlockRenderManagerMixin {

    /**
     * Corrigido: A assinatura do método renderBlock foi atualizada para corresponder aos mapeamentos do Yarn 1.21.6.
     * O parâmetro Random random foi substituído por List<BlockModelPart> parts.
     */
    @Inject(
        // Assinatura corrigida: o último parâmetro Random random foi substituído por List<BlockModelPart> parts
        method = "renderBlock(Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/world/BlockRenderView;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;ZLjava/util/List;)V",
        at = @At("HEAD"),
        cancellable = true
    )
    // O método Java precisa corresponder à nova assinatura, recebendo List<BlockModelPart>
    private void barium$cullDenseFoliage(BlockState state, BlockPos pos, BlockRenderView world, MatrixStack matrices, VertexConsumer vertexConsumer, boolean cull, List<BlockModelPart> parts, CallbackInfo ci) { // Parâmetro Random random removido e List<BlockModelPart> adicionado
        if (!BariumConfig.C.ENABLE_DENSE_FOLIAGE_CULLING || BariumConfig.C.DENSE_FOLIAGE_CULLING_LEVEL <= 0) {
            return;
        }

        // A lógica de culling de folhagem densa ainda usa a BlockState, então ela deve funcionar.
        // Se for necessário acessar os 'parts', você pode fazê-lo aqui.
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