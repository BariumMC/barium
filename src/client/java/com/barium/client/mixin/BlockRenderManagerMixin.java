// src/client/java/com/barium/client/mixin/BlockRenderManagerMixin.java
package com.barium.client.mixin;

import com.barium.config.BariumConfig;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.model.BlockModelPart;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.List;

@Mixin(BlockRenderManager.class)
public class BlockRenderManagerMixin {

    /**
     * CORREÇÃO: A assinatura do método foi revertida para a versão correta com 'List',
     * pois a versão com 'Random' não é a correta para esta injeção.
     */
    @Inject(
        method = "renderBlock(Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/world/BlockRenderView;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;ZLjava/util/List;)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void barium$cullDenseFoliage(BlockState state, BlockPos pos, BlockRenderView world, MatrixStack matrices, VertexConsumer vertexConsumer, boolean cull, List<BlockModelPart> parts, CallbackInfo ci) {
        int level = BariumConfig.C.DENSE_FOLIAGE_CULLING_LEVEL;
        if (!BariumConfig.C.ENABLE_DENSE_FOLIAGE_CULLING || level <= 0) {
            return;
        }

        if (isTargetFoliage(state)) {
            long seed = (long)pos.getX() * 3129871 ^ (long)pos.getZ() * 1125899;
            seed = seed * seed * 4231761 + seed * 11;
            int hash = (int)(seed >> 16);

            int cullChance;
            switch (level) {
                case 1: cullChance = 25; break;
                case 2: cullChance = 50; break;
                case 3: cullChance = 75; break;
                case 4: cullChance = 90; break;
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
               // CORREÇÃO: Corrigido erro de digitação de 'isof' para 'isOf'.
               state.isOf(Blocks.LILY_OF_THE_VALLEY) ||
               state.isOf(Blocks.BROWN_MUSHROOM) ||
               state.isOf(Blocks.RED_MUSHROOM);
    }
}