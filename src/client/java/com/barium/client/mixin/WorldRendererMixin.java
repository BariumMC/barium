package com.barium.client.mixin;

import com.barium.BariumMod;
import com.barium.client.optimization.ClientTerrainOptimizer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.chunk.ChunkBuilder.BuiltChunk;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.WorldChunk;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin {

    @Shadow
    private MinecraftClient client;

    @Inject(method = "renderChunk", at = @At("HEAD"), cancellable = true)
    private void barium$onRenderChunk(BuiltChunk chunk, RenderLayer renderLayer, MatrixStack matrices, double x, double y, double z, Matrix4f projectionMatrix, CallbackInfo ci) {
        if (client.world == null || !com.barium.config.BariumConfig.ENABLE_TERRAIN_STREAMING) {
            return;
        }
        
        BlockPos origin = chunk.getOrigin(); // This method is correct for getting the origin BlockPos
        ChunkPos chunkPos = new ChunkPos(origin.getX() >> 4, origin.getZ() >> 4);
        
        WorldChunk worldChunk = client.world.getChunk(chunkPos.x, chunkPos.z);

        if (!ClientTerrainOptimizer.shouldRenderChunk(worldChunk, client.gameRenderer.getCamera())) {
            ci.cancel();
        }
    }
    
    @ModifyVariable(method = "setupTerrain", at = @At(value = "STORE", ordinal = 0), name = "builtChunk", allow = 1)
    private BuiltChunk barium$modifyBuiltChunkForRebuild(BuiltChunk builtChunk) {
        return builtChunk;
    }
}