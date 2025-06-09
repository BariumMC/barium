package com.barium.client.mixin;

import com.barium.config.BariumConfig;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockRenderView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.ThreadLocalRandom;

@Mixin(BlockRenderManager.class)
public class BlockRenderManagerMixin {

    @Inject(
        method = "renderBlock(Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/world/BlockRenderView;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;ZLnet/minecraft/util/math/random/Random;)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void barium$cullDenseFoliage(BlockState state, BlockPos pos, BlockRenderView world, MatrixStack matrices, VertexConsumer vertexConsumer, boolean cull, Random random, CallbackInfo ci) {
        if (!BariumConfig.ENABLE_DENSE_FOLIAGE_CULLING || BariumConfig.DENSE_FOLIAGE_CULLING_LEVEL <= 0) {
            return;
        }

        // Verificamos se é um dos blocos que queremos "desbastar"
        if (isDenseFoliage(state)) {
            // Nível 1: Pula 1 em 4 (75% densidade)
            // Nível 2: Pula 2 em 4 (50% densidade)
            // Nível 3: Pula 3 em 4 (25% densidade)
            if (ThreadLocalRandom.current().nextInt(4) < BariumConfig.DENSE_FOLIAGE_CULLING_LEVEL) {
                ci.cancel(); // Pula a renderização deste bloco de grama/arbusto
            }
        }
    }

    private boolean isDenseFoliage(BlockState state) {
        // CORREÇÃO: Usando a referência correta para a grama.
        // Lista de blocos que consideramos "vegetação densa"
        return state.isOf(Blocks.SHORT_GRASS) || // O nome correto para a grama plantada
               state.isOf(Blocks.FERN) ||
               state.isOf(Blocks.TALL_GRASS) ||
               state.isOf(Blocks.LARGE_FERN) ||
               state.isOf(Blocks.DEAD_BUSH) ||
               state.isOf(Blocks.VINE);
    }
}