package com.barium.client.mixin;

import com.barium.client.optimization.FoliageOptimizer;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.block.BlockModelRenderer;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockRenderView;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;

@Mixin(BlockModelRenderer.class)
public abstract class BlockModelRendererMixin {

    @Unique
    private FoliageOptimizer.FoliageLod barium$currentLod = FoliageOptimizer.FoliageLod.FULL;
    @Unique
    private BlockPos barium$currentPos;
    @Unique
    private int barium$quadCounter = 0;

    @Inject(
        method = "render(Lnet/minecraft/world/BlockRenderView;Lnet/minecraft/client/render/model/BakedModel;Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;ZLnet/minecraft/util/math/random/Random;JI)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/model/BakedModel;getQuads(Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/Direction;Lnet/minecraft/util/math/random/Random;)Ljava/util/List;"),
        locals = LocalCapture.CAPTURE_FAILHARD,
        require = 0
    )
    private void barium$beforeRenderQuads(BlockRenderView world, Object model, BlockState state, BlockPos pos, MatrixStack matrices, VertexConsumer vertexConsumer, boolean cull, Random random, long seed, int overlay, CallbackInfo ci) {
        this.barium$currentPos = pos;
        this.barium$currentLod = FoliageOptimizer.getFoliageLod(state, pos);
        this.barium$quadCounter = 0;
    }

    @Inject(
        method = "renderQuad(Lnet/minecraft/client/util/math/MatrixStack$Entry;Lnet/minecraft/client/render/VertexConsumer;FFFLjava/util/List;II)V",
        at = @At("HEAD"),
        cancellable = true,
        require = 0
    )
    private void barium$onRenderQuad(MatrixStack.Entry entry, VertexConsumer consumer, float red, float green, float blue, List<BakedQuad> quads, int light, int overlay, CallbackInfo ci) {
        if (this.barium$currentLod == FoliageOptimizer.FoliageLod.FULL) return;

        BakedQuad quad = quads.get(0);

        // CORREÇÃO DO ERRO DE DIGITAÇÃO
        if (this.barium$currentLod == FoliageOptimizer.FoliageLod.CULLED) {
            ci.cancel();
            return;
        }

        if (this.barium$currentLod == FoliageOptimizer.FoliageLod.FLAT) {
            if (this.barium$quadCounter >= 2) {
                ci.cancel();
                return;
            }
        }

        if (this.barium$currentLod == FoliageOptimizer.FoliageLod.CROSS_WITH_CULLING) {
            // CORREÇÃO DO MÉTODO
            Direction faceDirection = quad.getSide();
            if (faceDirection != null && (faceDirection.getAxis().isHorizontal() || faceDirection.getAxis().isVertical())) {
                Vec3d cameraDirection = MinecraftClient.getInstance().gameRenderer.getCamera().getPos().subtract(this.barium$currentPos.toCenterPos()).normalize();
                Vector3f faceNormal = faceDirection.getUnitVector();
                if (cameraDirection.dotProduct(new Vec3d(faceNormal.x(), faceNormal.y(), faceNormal.z())) > 0.1) {
                    ci.cancel();
                    return;
                }
            }
        }

        this.barium$quadCounter++;
    }
}