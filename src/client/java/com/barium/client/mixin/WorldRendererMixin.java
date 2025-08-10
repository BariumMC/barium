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
     * A tomada de controle final.
     * Interceptamos CADA chamada para `WorldRenderer.renderLayer` que o jogo faz.
     * Em vez de deixar o vanilla renderizar, chamamos nosso próprio sistema.
     * Isso é robusto porque não dependemos da assinatura de `render()`, apenas de `renderLayer()`.
     */
    @Redirect(
        method = "render(Lnet/minecraft/client/util/math/MatrixStack;FJZLnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/GameRenderer;Lnet/minecraft/client/render/LightmapTextureManager;Lorg/joml/Matrix4f;)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/render/WorldRenderer;renderLayer(Lnet/minecraft/client/render/RenderLayer;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/Camera;)V"
        )
    )
    private void barium$redirectRenderLayer(WorldRenderer instance, RenderLayer layer, MatrixStack matrices, Camera camera) {
        // O jogo nos dá o layer correto (SOLID, CUTOUT, etc.).
        // Nós simplesmente o pegamos e o desenhamos com nosso sistema.
        BariumRenderManager.getInstance().renderLayer(layer, matrices, camera, this.frustum);

        // O render vanilla para este layer é pulado porque não chamamos `instance.renderLayer(...)`.
    }
}
