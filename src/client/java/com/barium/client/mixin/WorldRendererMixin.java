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

    @Inject(method = "setWorld", at = @At("HEAD"))
    private void barium$onSetWorld(@Nullable ClientWorld newWorld, CallbackInfo ci) {
        BariumRenderManager.getInstance().onWorldChange(newWorld);
    }

    @Inject(method = "scheduleChunkRender(IIIZ)V", at = @At("HEAD"), cancellable = true)
    private void barium$takeOverRebuildScheduling(int x, int y, int z, boolean isPriority, CallbackInfo ci) {
        BariumRenderManager.getInstance().scheduleRebuild(x, y, z, isPriority);
        ci.cancel();
    }
    
    /**
     * CORREÇÃO FINAL E DEFINITIVA: O nome do método alvo foi corrigido para 'renderBlocksLayer',
     * exatamente como você apontou. Esta é a correção que faz a otimização de chunks funcionar.
     */
    @Inject(
        method = "renderBlockLayers",
        at = @At("HEAD"),
        cancellable = true
    )
    private void barium$renderChunkLayer(RenderLayer renderLayer, MatrixStack matrices, double cameraX, double cameraY, double cameraZ, Matrix4f positionMatrix, CallbackInfo ci) {
        boolean isHandled = BariumRenderManager.getInstance().renderLayer(renderLayer, matrices, cameraX, cameraY, cameraZ, this.frustum);
        if (isHandled) {
            ci.cancel();
        }
    }
}