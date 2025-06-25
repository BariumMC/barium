package com.barium.client.mixin;

import com.barium.config.BariumConfig;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.model.BlockModelPart; // Embora o Mixin use List, o tipo Java pode ser específico
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List; // Import necessário
import java.util.concurrent.ThreadLocalRandom;

@Mixin(BlockRenderManager.class)
public class BlockRenderManagerMixin {

    /**
     * Injeta no início do método que renderiza um modelo de bloco.
     * CONFIRMADO: A assinatura do método `renderBlock` foi atualizada para a versão 1.21.6,
     * usando a informação exata do seletor de Mixin.
     */
    @Inject(
        // Usando o seletor exato que você forneceu.
        method = "renderBlock(Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/world/BlockRenderView;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;ZLjava/util/List;)V",
        at = @At("HEAD"),
        cancellable = true
    )
    // O método Java deve corresponder à assinatura do Mixin.
    // Usamos List<?> para máxima compatibilidade, mas List<BlockModelPart> também funciona se a classe for visível.
    private void barium$cullDenseFoliage(BlockState state, BlockPos pos, BlockRenderView world, MatrixStack matrices, VertexConsumer vertexConsumer, boolean cull, List<?> parts, CallbackInfo ci) {
        if (!BariumConfig.C.ENABLE_DENSE_FOLIAGE_CULLING || BariumConfig.C.DENSE_FOLIAGE_CULLING_LEVEL <= 0) {
            return;
        }

        if (isTargetFoliage(state)) {
            if (ThreadLocalRandom.current().nextInt(4) < BariumConfig.C.DENSE_FOLIAGE_CULLING_LEVEL) {
                ci.cancel();
            }
        }
    }

    private boolean isTargetFoliage(BlockState state) {
        return state.isOf(Blocks.SHORT_GRASS) ||
               state.isOf(Blocks.FERN) ||
               state.isOf(Blocks.TALL_GRASS) ||
               state.isOf(Blocks.LARGE_FERN) ||
               state.isOf(Blocks.DEAD_BUSH) ||
               state.isOf(Blocks.VINE) ||
               state.isOf(Blocks.LILY_PAD) ||
               state.isOf(Blocks.DANDELION) ||
               state.isOf(Blocks.POPPY) ||
               state.isOf(Blocks.BLUE_ORCHID) ||
               state.isOf(Blocks.ALLIUM) ||
               state.isOf(Blocks.AZURE_BLUET) ||
               state.isOf(Blocks.RED_TULIP) ||
               state.isOf(Blocks.ORANGE_TULIP) ||
               state.isOf(Blocks.WHITE_TULIP) ||
               state.isOf(Blocks.PINK_TULIP) ||
               state.isOf(Blocks.OXEYE_DAISY) ||
               state.isOf(Blocks.CORNFLOWER) ||
               state.isOf(Blocks.LILY_OF_THE_VALLEY) ||
               state.isOf(Blocks.BROWN_MUSHROOM) ||
               state.isOf(Blocks.RED_MUSHROOM);
    }
}