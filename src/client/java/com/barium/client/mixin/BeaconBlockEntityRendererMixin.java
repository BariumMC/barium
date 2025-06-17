package com.barium.client.mixin;

import com.barium.config.BariumConfig;
import net.minecraft.block.entity.BeaconBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BeaconBlockEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d; // Importe Vec3d
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BeaconBlockEntityRenderer.class)
public class BeaconBlockEntityRendererMixin {

    /**
     * Otimização para não renderizar raios de beacon distantes.
     * Alvo: BeaconBlockEntityRenderer.render(...)
     */
    @Inject(
        // CORREÇÃO: A assinatura foi atualizada para incluir o novo parâmetro 'Vec3d cameraPos'
        // Descritor para: render(BeaconBlockEntity, float, MatrixStack, VertexConsumerProvider, int, int, Vec3d)
        method = "render(Lnet/minecraft/block/entity/BeaconBlockEntity;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IILnet/minecraft/util/math/Vec3d;)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void barium$cullDistantBeaconBeams(BeaconBlockEntity beacon, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, Vec3d cameraPos, CallbackInfo ci) {
        // Verifica se a otimização está ativa
        if (!BariumConfig.C.ENABLE_BEACON_BEAM_CULLING) {
            return;
        }

        // Não precisamos mais pegar a câmera de MinecraftClient.getInstance(), pois agora ela é um parâmetro!
        // Isto é mais limpo e mais seguro.
        if (cameraPos == null) return;

        // Calcula a distância ao quadrado da câmera (que foi passada como parâmetro) até o beacon.
        // Usamos Vec3d.ofCenter() para obter o centro do bloco, o que é mais preciso.
        double distanceSq = beacon.getPos().toCenterPos().squaredDistanceTo(cameraPos);

        // Se a distância for maior que a configurada, cancela a renderização.
        if (distanceSq > BariumConfig.C.BEACON_BEAM_CULL_DISTANCE_SQ) {
            ci.cancel();
        }
    }
}