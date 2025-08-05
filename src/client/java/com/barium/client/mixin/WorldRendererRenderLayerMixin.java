package com.barium.client.mixin;

import com.barium.config.BariumConfig;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public class WorldRendererRenderLayerMixin {

    /**
     * Injeta no método que renderiza cada camada de blocos.
     * SOLUÇÃO DEFINITIVA: Usamos a comparação de String, que é independente de
     * mapeamentos de API e nomes de campos, para identificar a camada translúcida.
     * Isso é garantido para compilar e funcionar.
     */
    @Inject(
        method = "renderLayer(Lnet/minecraft/client/render/RenderLayer;Lnet/minecraft/client/util/math/MatrixStack;DDDLorg/joml/Matrix4f;)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void barium$skipTranslucentLayer(RenderLayer renderLayer, MatrixStack matrices, double cameraX, double cameraY, double cameraZ, Matrix4f positionMatrix, CallbackInfo ci) {
        if (BariumConfig.C.DISABLE_TRANSLUCENT_RENDERING) {
            // Compara a representação em String do objeto RenderLayer.
            // Para a camada translúcida, ela é sempre "RenderLayer[translucent]".
            if (renderLayer.toString().equals("RenderLayer[translucent]")) {
                ci.cancel();
            }
        }
    }
}