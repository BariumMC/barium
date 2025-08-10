package com.barium.client.mixin;

import com.barium.client.render.BariumRenderManager;
import com.mojang.blaze3d.systems.RenderSystem;
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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Novos imports para a API FrameGraph
import net.minecraft.client.render.object.ObjectAllocator;
import net.minecraft.client.util.GpuBufferSlice;
import org.joml.Vector4f;

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
     * Ponto de injeção final e correto.
     * Injetamos no final do método `render` com a assinatura `FrameGraph` que o crash report confirmou.
     * Desenhamos nosso mundo por cima do mundo vanilla (que estará vazio).
     */
    @Inject(
        method = "render(Lnet/minecraft/client/render/object/ObjectAllocator;Lnet/minecraft/client/render/RenderTickCounter;ZLnet/minecraft/client/render/Camera;Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;Lnet/minecraft/client/util/GpuBufferSlice;Lorg/joml/Vector4f;Z)V",
        at = @At("TAIL")
    )
    private void barium$renderBariumWorld(
            ObjectAllocator allocator, RenderTickCounter tickCounter, boolean renderBlockOutline,
            Camera camera, Matrix4f positionMatrix, Matrix4f projectionMatrix,
            GpuBufferSlice fog, Vector4f fogColor, boolean shouldRenderSky,
            CallbackInfo ci) 
    {
        // Precisamos de um MatrixStack para o nosso renderizador. Vamos criar um a partir da matriz de posição.
        MatrixStack matrices = new MatrixStack();
        matrices.peek().getPositionMatrix().mul(positionMatrix);

        BariumRenderManager.getInstance().render(matrices, camera, this.frustum);
    }
}