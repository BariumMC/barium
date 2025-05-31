package com.barium.client.mixin;

import com.barium.client.optimization.TransparentBlockOptimizer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.Camera;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public abstract class TransparentBlockMixin {

    @Inject(
        method = "renderLayer"
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/render/RenderLayer;startDrawing()V",
            shift = At.Shift.BEFORE
        )
    )
    private void barium$beforeRenderTranslucentLayer(RenderLayer renderLayer, MatrixStack matrices, double cameraX, double cameraY, double cameraZ, Matrix4f positionMatrix, CallbackInfo ci) {
        if (renderLayer == RenderLayer.getTranslucent()) {
            WorldRenderer self = (WorldRenderer)(Object)this;
            Camera camera = MinecraftClient.getInstance().gameRenderer.getCamera();

            TransparentBlockOptimizer.optimizeTranslucentRendering(self, matrices, camera, cameraX, cameraY, cameraZ);
        }
    }
}