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

@Mixin(BlockRenderManager.class)
public class BlockRenderManagerMixin {

    /**
     * ATUALIZAÇÃO 25w45a: A assinatura do método `renderBlock` mudou drasticamente.
     * O mixin foi atualizado para corresponder à nova assinatura.
     */
    @Inject(
        method = "renderBlock(Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/world/BlockRenderView;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;ZLnet/minecraft/util/math/random/Random;J)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void barium$cullDenseFoliage(BlockState state, BlockPos pos, BlockRenderView world, MatrixStack matrices, VertexConsumer vertexConsumer, boolean cull, Random random, long seed, CallbackInfo ci) {
        int level = BariumConfig.C.DENSE_FOLIAGE_CULLING_LEVEL;
        if (!BariumConfig.C.ENABLE_DENSE_FOLIAGE_CULLING || level <= 0) {
            return;
        }

        if (isTargetFoliage(state)) {
            // Usa um hash posicional em vez de aleatório.
            long hashSeed = (long)pos.getX() * 3129871 ^ (long)pos.getZ() * 1125899;
            hashSeed = hashSeed * hashSeed * 4231761 + hashSeed * 11;
            int hash = (int)(hashSeed >> 16);

            int cullChance; // Chance em 100 de ser REMOVIDO
            switch (level) {
                case 1: cullChance = 25; break; // Leve
                case 2: cullChance = 50; break; // Médio
                case 3: cullChance = 75; break; // Pesado
                case 4: cullChance = 90; break; // Extremo
                default: return;
            }

            if ((Math.abs(hash) % 100) < cullChance) {
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