package com.barium.client.mixin;

import com.barium.client.render.BariumRenderManager;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin {

    @Shadow private @Nullable ClientWorld world;
    @Shadow private Frustum frustum;

    // Injeção para o Barium saber quando o mundo muda.
    @Inject(method = "setWorld", at = @At("HEAD"))
    private void barium$onSetWorld(@Nullable ClientWorld newWorld, CallbackInfo ci) {
        BariumRenderManager.getInstance().onWorldChange(newWorld);
    }

    // Intercepta o pedido de reconstrução de chunk e o envia para o sistema do Barium.
    @Inject(method = "scheduleChunkRender(IIIZ)V", at = @At("HEAD"), cancellable = true)
    private void barium$takeOverRebuildScheduling(int x, int y, int z, boolean isPriority, CallbackInfo ci) {
        BariumRenderManager.getInstance().scheduleRebuild(x, y, z, isPriority);
        ci.cancel(); // Impede o Minecraft de agendar a reconstrução, evitando trabalho duplicado.
    }
    
    // CORREÇÃO: Esta é a nova injeção cirúrgica.
    // Ela intercepta a renderização de cada camada de terreno (sólido, translúcido, etc.).
    @Inject(method = "renderLayer", at = @At("HEAD"), cancellable = true)
    private void barium$renderChunkLayer(RenderLayer renderLayer, MatrixStack matrices, double cameraX, double cameraY, double cameraZ, Matrix4f positionMatrix, CallbackInfo ci) {
        // Deixa o Barium desenhar a camada de chunk.
        boolean isHandled = BariumRenderManager.getInstance().renderLayer(renderLayer, matrices, cameraX, cameraY, cameraZ, this.frustum);

        // Se o Barium desenhou algo para esta camada, nós cancelamos a renderização original do Minecraft
        // para evitar que o mundo seja desenhado duas vezes.
        if (isHandled) {
            ci.cancel();
        }
    }
}