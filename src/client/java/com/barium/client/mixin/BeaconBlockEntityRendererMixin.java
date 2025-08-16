package com.barium.client.mixin;

import com.barium.config.BariumConfig;
import net.minecraft.client.render.block.entity.BeaconBlockEntityRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.block.entity.BeaconBlockEntity;
import net.minecraft.client.render.Camera;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.Vec3d;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BeaconBlockEntityRenderer.class)
public class BeaconBlockEntityRendererMixin {

    /**
     * CORREÇÃO: O seletor do método foi especificado para ser mais robusto.
     */
    @Inject(
        method = "render(Lnet/minecraft/block/entity/BeaconBlockEntity;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;II)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void barium$skipDistantBeaconBeams(BeaconBlockEntity beaconBlockEntity, 
                                             float tickDelta, 
                                             MatrixStack matrices, 
                                             VertexConsumerProvider vertexConsumers,
                                             int light, 
                                             int overlay, 
                                             CallbackInfo ci) {
        if (!BariumConfig.C.ENABLE_BEACON_BEAM_OPTIMIZATION) {
            return;
        }
        
        Vec3d beaconPos = Vec3d.ofCenter(beaconBlockEntity.getPos());
        Camera camera = MinecraftClient.getInstance().gameRenderer.getCamera();
        Vec3d cameraPos = camera.getPos();
        
        double dx = beaconPos.x - cameraPos.x;
        double dy = beaconPos.y - cameraPos.y;
        double dz = beaconPos.z - cameraPos.z;
        double distanceSq = dx * dx + dy * dy + dz * dz;
        
        if (distanceSq > BariumConfig.C.BEACON_BEAM_CULL_DISTANCE_SQ) {
            ci.cancel();
        }
    }
}