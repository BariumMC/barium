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

/**
 * Mixin para otimização de renderização de raios de beacon
 * Atualizado para Minecraft 1.21.6
 */
@Mixin(BeaconBlockEntityRenderer.class)
public class BeaconBlockEntityRendererMixin {

    /**
     * Otimização para pular a renderização de raios de beacon distantes
     */
    @Inject(method = "render*", at = @At("HEAD"), cancellable = true)
    private void barium$skipDistantBeaconBeams(BeaconBlockEntity beaconBlockEntity, 
                                             float tickDelta, 
                                             MatrixStack matrices, 
                                             VertexConsumerProvider vertexConsumers,
                                             int light, 
                                             int overlay, 
                                             CallbackInfo ci) {
        // Se a otimização estiver desativada, não faz nada
        if (!BariumConfig.C.ENABLE_BEACON_BEAM_OPTIMIZATION) {
            return;
        }
        
        // Obtém a posição do beacon e da câmera
        Vec3d beaconPos = Vec3d.ofCenter(beaconBlockEntity.getPos());
        Camera camera = MinecraftClient.getInstance().gameRenderer.getCamera();
        Vec3d cameraPos = camera.getPos();
        
        // Calcula a distância ao quadrado
        double dx = beaconPos.x - cameraPos.x;
        double dy = beaconPos.y - cameraPos.y;
        double dz = beaconPos.z - cameraPos.z;
        double distanceSq = dx * dx + dy * dy + dz * dz;
        
        // Se estiver muito longe, não renderiza o raio
        if (distanceSq > BariumConfig.C.BEACON_BEAM_CULL_DISTANCE_SQ) {
            ci.cancel();
        }
    }
}
