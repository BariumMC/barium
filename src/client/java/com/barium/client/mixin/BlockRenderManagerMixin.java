package com.barium.client.mixin;

import com.barium.config.BariumConfig;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.block.BlockRenderManager;
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

    @Inject(method = "renderBlock", at = @At("HEAD"), cancellable = true)
    private void barium$optimizedFoliageCulling(BlockState state, BlockPos pos, BlockRenderView world, MatrixStack matrices, VertexConsumer vertexConsumer, boolean cull, List<?> parts, CallbackInfo ci) {
        int level = BariumConfig.C.DENSE_FOLIAGE_CULLING_LEVEL;
        if (BariumConfig.C.ENABLE_DENSE_FOLIAGE_CULLING && level > 0) {
            if (state.isIn(net.minecraft.registry.tag.BlockTags.LEAVES) || state.isOf(Blocks.SHORT_GRASS)) {
                int h = (pos.getX() * 3129871) ^ (pos.getZ() * 116129781) ^ pos.getY();
                h = (h ^ (h >>> 16));

                int chance = switch (level) {
                    case 1 -> 20;
                    case 2 -> 40;
                    case 3 -> 70;
                    case 4 -> 90;
                    default -> 0;
                };

                if ((h & 0x7FFFFFFF) % 100 < chance) {
                    ci.cancel();
                    return;
                }
            }
        }

        if (!BariumConfig.C.ENABLE_DISTANT_GEOMETRY_CULLING || !BariumConfig.C.ENABLE_AGGRESSIVE_DISTANT_DETAIL_CULLING) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) {
            return;
        }

        if (!isDetailBlock(state.getBlock())) {
            return;
        }

        int threshold = Math.max(16, BariumConfig.C.DISTANT_DETAIL_CULL_DISTANCE);
        double dx = client.player.getX() - (pos.getX() + 0.5);
        double dy = client.player.getY() - (pos.getY() + 0.5);
        double dz = client.player.getZ() - (pos.getZ() + 0.5);
        double distSq = dx * dx + dy * dy + dz * dz;
        if (distSq <= (double) threshold * threshold) {
            return;
        }

        int h = stableHash(pos);
        int keepChance = 55;

        if (distSq > (double) (threshold * 2) * (threshold * 2)) {
            keepChance = state.isIn(net.minecraft.registry.tag.BlockTags.LEAVES) ? 20 : 0;
        }

        if ((h & 0x7FFFFFFF) % 100 >= keepChance) {
            ci.cancel();
        }
    }

    private static int stableHash(BlockPos pos) {
        int h = (pos.getX() * 73428767) ^ (pos.getY() * 912931) ^ (pos.getZ() * 438289);
        return h ^ (h >>> 16);
    }

    private static boolean isDetailBlock(Block block) {
        return block == Blocks.SHORT_GRASS
                || block == Blocks.TALL_GRASS
                || block == Blocks.FERN
                || block == Blocks.LARGE_FERN
                || block == Blocks.VINE
                || block == Blocks.TWISTING_VINES
                || block == Blocks.WEEPING_VINES
                || block instanceof net.minecraft.block.FlowerBlock
                || block instanceof net.minecraft.block.LeavesBlock;
    }
}
