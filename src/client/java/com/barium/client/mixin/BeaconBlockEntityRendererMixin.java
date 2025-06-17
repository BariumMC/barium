package com.barium.client.mixin;

import com.barium.config.BariumConfig;
import net.minecraft.block.entity.BeaconBlockEntity;
import net.minecraft.block.entity.BlockEntity; // Importe BlockEntity
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
        // CORREÇÃO: Usando a superclasse BlockEntity devido ao type erasure de genéricos.
        // Descritor para: render(BlockEntity, float, MatrixStack, VertexConsumerProvider, int, int, Vec3d)
        method = "render(Lnet/minecraft/block/entity/BlockEntity;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IILnet/minecraft/util/math/Vec3d;)V",
        at = @At("HEAD"),
        cancellable = true
    )
    // A assinatura do método Java pode manter BeaconBlockEntity, pois o Mixin fará o cast.
    private void barium$cullDistantBeaconBeams(BeaconBlockEntity beacon, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, Vec3d cameraPos, CallbackInfo ci) {
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