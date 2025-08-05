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
     * Se a opção estiver ativa, ele cancela a renderização da camada translúcida.
     * CORREÇÃO: A assinatura do método está correta e RenderLayer.TRANSLUCENT é usado.
     */
    @Inject(
        method = "renderLayer(Lnet/minecraft/client/render/RenderLayer;Lnet/minecraft/client/util/math/MatrixStack;DDDLorg/joml/Matrix4f;)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void barium$skipTranslucentLayer(RenderLayer renderLayer, MatrixStack matrices, double cameraX, double cameraY, double cameraZ, Matrix4f positionMatrix, CallbackInfo ci) {
        // Se a opção estiver ativa e a camada for translúcida...
        if (BariumConfig.C.DISABLE_TRANSLUCENT_RENDERING && renderLayer == RenderLayer.TRANSLUCENT) {
            ci.cancel(); // ...cancela a renderização desta camada inteira.
        }
    }
}