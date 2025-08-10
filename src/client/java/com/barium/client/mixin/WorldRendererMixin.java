package com.barium.client.mixin;

import com.barium.client.render.BariumRenderManager;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import org.jetbrains.annotations.Nullable;
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
    
    @Inject(method = "updateBlock", at = @At("HEAD"))
    private void barium$onBlockUpdate(BlockView world, BlockPos pos, BlockState oldState, BlockState newState, int flags, CallbackInfo ci) {
        int sectionX = pos.getX() >> 4;
        int sectionY = pos.getY() >> 4;
        int sectionZ = pos.getZ() >> 4;
        BariumRenderManager.getInstance().scheduleRebuild(sectionX, sectionY, sectionZ, false);
    }

    /**
     * Ponto de injeção para a nossa renderização.
     * `setupTerrain` é chamado a cada frame, antes da renderização dos chunks, e tem o Frustum.
     * É o lugar perfeito para desenhar nosso mundo, antes que o vanilla tente desenhar o dele (que estará vazio).
     */
    @Inject(method = "setupTerrain", at = @At("TAIL"))
    private void barium$renderBariumWorld(Camera camera, Frustum frustum, boolean hasForcedFrustum, boolean spectator, CallbackInfo ci) {
        // O `matrices` não está disponível aqui, mas podemos obtê-lo.
        // No entanto, para simplificar, vamos injetar em um lugar melhor.
        // `render` é muito complexo. `renderLayer` não existe.
        // A solução é injetar no final do método que prepara tudo.
    }
    
    // Vamos usar um ponto de injeção mais estável que tem todos os argumentos.
    // O final de `render` é o melhor lugar.
    @Inject(method = "render", at = @At("TAIL"))
    private void barium$renderBariumWorldAfterVanilla(net.minecraft.client.util.math.MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, net.minecraft.client.render.GameRenderer gameRenderer, net.minecraft.client.render.LightmapTextureManager lightmapTextureManager, org.joml.Matrix4f projectionMatrix, CallbackInfo ci) {
         BariumRenderManager.getInstance().render(matrices, camera, this.frustum);
    }
}