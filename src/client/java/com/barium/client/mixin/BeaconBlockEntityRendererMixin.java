// --- Mantenha o código do seu BeaconBlockEntityRendererMixin.java como está ---
// (Última versão com a assinatura corrigida)
package com.barium.client.mixin;

import com.barium.config.BariumConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BeaconBlockEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.block.entity.BeaconBlockEntity;
import net.minecraft.util.math.Vec3d; // Import necessário para Vec3d
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BeaconBlockEntityRenderer.class)
public class BeaconBlockEntityRendererMixin {

    /**
     * Corrigido: A assinatura do método render foi atualizada para incluir Vec3d cameraPos
     * e a ordem dos parâmetros light/overlay foi ajustada conforme a documentação.
     */
    @Inject(
        // Assinatura que parece correta com base na documentação fornecida.
        // Se o erro persistir, pode ser um problema de mapeamento Yarn sutil ou configuração do Mixin.
        method = "render(Lnet/minecraft/block/entity/BeaconBlockEntity;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IILnet/minecraft/util/math/Vec3d;)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void barium$cullDistantBeaconBeams(BeaconBlockEntity beacon, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, Vec3d cameraPos, CallbackInfo ci) { // Adicionado Vec3d cameraPos como parâmetro
        if (!BariumConfig.C.ENABLE_BEACON_BEAM_CULLING) {
            return;
        }
        
        double distanceSq = beacon.getPos().getSquaredDistance(cameraPos); // Usamos o cameraPos passado

        if (distanceSq > BariumConfig.C.BEACON_BEAM_CULL_DISTANCE_SQ) {
            ci.cancel();
        }
    }
}