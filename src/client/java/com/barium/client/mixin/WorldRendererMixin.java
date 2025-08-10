package com.barium.client.mixin;

import com.barium.client.render.BariumRenderManager;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
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
    
    @Inject(method = "updateBlock", at = @At("HEAD"))
    private void barium$onBlockUpdate(BlockView world, BlockPos pos, BlockState oldState, BlockState newState, int flags, CallbackInfo ci) {
        int sectionX = pos.getX() >> 4;
        int sectionY = pos.getY() >> 4;
        int sectionZ = pos.getZ() >> 4;
        BariumRenderManager.getInstance().scheduleRebuild(sectionX, sectionY, sectionZ, false);
    }
    
    /**
     * Injeta no final do método de renderização principal e desenha nosso mundo.
     * Isso acontece depois que o vanilla tentou (e falhou em) desenhar os chunks.
     */
    @Inject(
        method = "render(Lnet/minecraft/client/util/math/MatrixStack;FJZLnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/GameRenderer;Lnet/minecraft/client/render/LightmapTextureManager;Lorg/joml/Matrix4f;)V",
        at = @At("TAIL")
    )
    private void barium$renderOurWorld(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f projectionMatrix, CallbackInfo ci) {
        BariumRenderManager.getInstance().render(matrices, camera, this.frustum);
    }
}