package com.barium.client.mixin;

import com.barium.config.BariumConfig;
import net.minecraft.block.entity.BeaconBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BeaconBlockEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BeaconBlockEntityRenderer.class)
public class BeaconBlockEntityRendererMixin {

    @Inject(
        method = "render(Lnet/minecraft/block/entity/BeaconBlockEntity;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;II)V", // Keep this signature if it's confirmed correct, otherwise update
        at = @At("HEAD"),
        cancellable = true
    )
    private void barium$cullDistantBeaconBeams(BeaconBlockEntity beacon, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, CallbackInfo ci) {
        if (!BariumConfig.C.ENABLE_BEACON_BEAM_CULLING) {
            return;
        }

        Camera camera = MinecraftClient.getInstance().gameRenderer.getCamera();
        if (camera == null) return;

        double distanceSq = beacon.getPos().getSquaredDistance(camera.getPos());

        // Use the configured squared distance.
        if (distanceSq > BariumConfig.C.BEACON_BEAM_CULL_DISTANCE_SQ) {
            ci.cancel();
        }
    }
}