package com.barium.client.mixin;

import com.barium.config.BariumConfig;
import net.minecraft.block.entity.BeaconBlockEntity;
import net.minecraft.block.entity.BlockEntity; // Import the base class
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BeaconBlockEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BeaconBlockEntityRenderer.class)
public class BeaconBlockEntityRendererMixin {

    @Inject(
        // The method descriptor is correct, targeting the erased generic method.
        method = "render(Lnet/minecraft/block/entity/BlockEntity;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IILnet/minecraft/util/math/Vec3d;)V",
        at = @At("HEAD"),
        cancellable = true
    )
    // CRUCIAL FIX: The handler method's signature must now accept the base type 'BlockEntity'.
    private void barium$cullDistantBeaconBeams(BlockEntity blockEntity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, Vec3d cameraPos, CallbackInfo ci) {
        // Now, we must check if the provided blockEntity is actually a beacon.
        // This is necessary because the renderer could theoretically be used for other block entities in the future.
        if (!(blockEntity instanceof BeaconBlockEntity beacon)) {
            return;
        }

        // The rest of the logic uses the 'beacon' variable, which is now safely cast.

        if (!BariumConfig.C.ENABLE_BEACON_BEAM_CULLING) {
            return;
        }

        if (cameraPos == null) return;

        double distanceSq = beacon.getPos().toCenterPos().squaredDistanceTo(cameraPos);

        if (distanceSq > BariumConfig.C.BEACON_BEAM_CULL_DISTANCE_SQ) {
            ci.cancel();
        }
    }
}
