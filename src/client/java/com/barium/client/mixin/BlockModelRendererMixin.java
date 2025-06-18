package com.barium.client.mixin;

import com.barium.client.optimization.FoliageOptimizer;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.block.BlockModelRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockRenderView;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(BlockModelRenderer.class)
public class BlockModelRendererMixin {

    private static FoliageOptimizer.FoliageLod currentLod = FoliageOptimizer.FoliageLod.FULL;
    private static BlockPos currentPos;
    private static int quadCounter = 0;

    @Inject(
        method = "render(Lnet/minecraft/world/BlockRenderView;Lnet/minecraft/client/render/model/BakedModel;Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;ZLnet/minecraft/util/math/random/Random;JI)V",
        at = @At("HEAD")
    )
    private void barium$beforeRenderModel(BlockRenderView world, BakedModel model, BlockState state, BlockPos pos, MatrixStack matrices, VertexConsumer vertexConsumer, boolean cull, Random random, long seed, int overlay, CallbackInfo ci) {
        currentPos = pos;
        currentLod = FoliageOptimizer.getFoliageLod(state, pos);
        quadCounter = 0;
    }

    @Inject(
        method = "renderQuad(Lnet/minecraft/client/util/math/MatrixStack$Entry;Lnet/minecraft/client/render/VertexConsumer;FFFLjava/util/List;II)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void barium$onRenderQuad(MatrixStack.Entry entry, VertexConsumer consumer, float red, float green, float blue, List<BakedQuad> quads, int light, int overlay, CallbackInfo ci) {
        if (currentLod == FoliageOptimizer.FoliageLod.FULL) return;

        BakedQuad quad = quads.get(0);

        if (currentLod == FoliageOptimizer.FoliageLod.CULLED) {
            ci.cancel();
            return;
        }

        if (currentLod == FoliageOptimizer.FoliageLod.FLAT) {
            if (quadCounter >= 2) {
                ci.cancel();
                return;
            }
        }

        if (currentLod == FoliageOptimizer.FoliageLod.CROSS_WITH_CULLING) {
            Direction faceDirection = quad.getFace();
            if (faceDirection.getAxis().isHorizontal() || faceDirection.getAxis().isVertical()) {
                Vec3d cameraDirection = MinecraftClient.getInstance().gameRenderer.getCamera().getPos().subtract(currentPos.toCenterPos()).normalize();
                Vector3f faceNormal = faceDirection.getUnitVector();
                if (cameraDirection.dotProduct(new Vec3d(faceNormal.x(), faceNormal.y(), faceNormal.z())) > 0.0) {
                    ci.cancel();
                    return;
                }
            }
        }

        quadCounter++;
    }
}