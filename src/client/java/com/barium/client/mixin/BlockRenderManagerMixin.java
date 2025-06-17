package com.barium.client.mixin;

import com.barium.config.BariumConfig;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.model.BlockModelPart; // Import BlockModelPart
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List; // Import List
import java.util.concurrent.ThreadLocalRandom;

@Mixin(BlockRenderManager.class)
public class BlockRenderManagerMixin {

    /**
     * Culls dense foliage by randomly skipping rendering for some foliage blocks.
     * Targets the updated renderBlock method.
     */
    @Inject(
        // CORRECTED SIGNATURE: The last parameter is now a List, not a Random.
        // Descriptor for: renderBlock(BlockState, BlockPos, BlockRenderView, MatrixStack, VertexConsumer, boolean, List)
        method = "renderBlock(Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/world/BlockRenderView;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;ZLjava/util/List;)V",
        at = @At("HEAD"),
        cancellable = true
    )
    // The handler method signature is updated to match. We can ignore the 'parts' list if not needed.
    private void barium$cullDenseFoliage(BlockState state, BlockPos pos, BlockRenderView world, MatrixStack matrices, VertexConsumer vertexConsumer, boolean cull, List<BlockModelPart> parts, CallbackInfo ci) {
        if (!BariumConfig.C.ENABLE_DENSE_FOLIAGE_CULLING || BariumConfig.C.DENSE_FOLIAGE_CULLING_LEVEL <= 0) {
            return;
        }

        if (isDenseFoliage(state)) {
            // We no longer have a Random object, so we must use our own.
            // ThreadLocalRandom is a good choice for this.
            if (ThreadLocalRandom.current().nextInt(4) < BariumConfig.C.DENSE_FOLIAGE_CULLING_LEVEL) {
                ci.cancel();
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