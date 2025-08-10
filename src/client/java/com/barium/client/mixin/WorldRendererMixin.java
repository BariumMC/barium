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
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin {

    // A assinatura desses @Shadows deve corresponder exatamente ao que está na sua versão do jogo.
    // Se eles ainda derem warning, significa que a assinatura mudou.
    @Shadow private @Nullable ClientWorld world;
    @Shadow private Frustum frustum;
    @Shadow protected abstract void renderSky(MatrixStack matrices, Matrix4f projectionMatrix, float tickDelta, Camera camera, boolean bl);
    @Shadow protected abstract void renderClouds(MatrixStack matrices, Matrix4f projectionMatrix, float tickDelta, double cameraX, double cameraY, double cameraZ);
    @Shadow protected abstract void renderWorldBorder(Camera camera);
    
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
     * @author Barium
     * @reason Substituição completa do método de renderização principal.
     *          Esta é a abordagem mais robusta para garantir controle total.
     */
    @Overwrite
    public void render(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f projectionMatrix) {
        
        // Desenha o fundo usando os métodos vanilla
        this.renderSky(matrices, projectionMatrix, tickDelta, camera, false);
        this.renderClouds(matrices, projectionMatrix, tickDelta, camera.getPos().x, camera.getPos().y, camera.getPos().z);
        
        // Chama nosso renderizador para desenhar os chunks
        BariumRenderManager.getInstance().renderWorld(matrices, camera, this.frustum);
        
        // Desenha o primeiro plano
        this.renderWorldBorder(camera);
    }
}