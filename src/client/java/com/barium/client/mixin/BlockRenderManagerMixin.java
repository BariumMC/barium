package com.barium.client.mixin;

import com.barium.config.BariumConfig;
import net.minecraft.block.BlockState;
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
        if (!BariumConfig.C.ENABLE_DENSE_FOLIAGE_CULLING || level <= 0) return;

        if (state.isIn(net.minecraft.registry.tag.BlockTags.LEAVES) || state.isOf(net.minecraft.block.Blocks.SHORT_GRASS)) {
            // Hash posicional usando bitwise para performance máxima
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
            }
        }
    }
}