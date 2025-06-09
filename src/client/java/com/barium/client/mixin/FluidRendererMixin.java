package com.barium.client.mixin;

import com.barium.config.BariumConfig;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.block.FluidRenderer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import java.util.concurrent.ThreadLocalRandom;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FluidRenderer.class)
public class FluidRendererMixin {

    @Inject(
        method = "render(Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/client/render/VertexConsumer;Lnet/minecraft/block/BlockState;Lnet/minecraft/fluid/FluidState;)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void barium$cullDenseFoliage(BlockView world, BlockPos pos, VertexConsumer vertexConsumer, BlockState blockState, net.minecraft.fluid.FluidState fluidState, CallbackInfo ci) {
        if (!BariumConfig.ENABLE_DENSE_FOLIAGE_CULLING || BariumConfig.DENSE_FOLIAGE_CULLING_LEVEL <= 0) {
            return;
        }

        // Esta otimização funciona em blocos que não são 'collidable' (atravessáveis)
        if (blockState.getCollisionShape(world, pos).isEmpty()) {
            // Verificamos se é um dos blocos que queremos "desbastar"
            if (isDenseFoliage(blockState)) {
                // Nível 1: Pula 1 em 4 (75% densidade)
                // Nível 2: Pula 2 em 4 (50% densidade)
                // Nível 3: Pula 3 em 4 (75% densidade)
                if (ThreadLocalRandom.current().nextInt(4) < BariumConfig.DENSE_FOLIAGE_CULLING_LEVEL) {
                    ci.cancel(); // Pula a renderização deste bloco de grama/arbusto
                }
            }
        }
    }

    private boolean isDenseFoliage(BlockState state) {
        // Lista de blocos que consideramos "vegetação densa"
        return state.isOf(Blocks.GRASS) ||
               state.isOf(Blocks.FERN) ||
               state.isOf(Blocks.TALL_GRASS) ||
               state.isOf(Blocks.LARGE_FERN) ||
               state.isOf(Blocks.DEAD_BUSH) ||
               state.isOf(Blocks.VINE);
        // Pode adicionar mais blocos aqui, como flores, etc.
    }
}