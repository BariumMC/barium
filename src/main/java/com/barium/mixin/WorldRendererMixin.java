package com.barium.mixin.optimization;

import com.barium.client.optimization.chunkculling.ChunkSectionVisibilityManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {

    @Inject(method = "render", at = @At(value = "HEAD"))
    private void barium$updateChunkVisibility(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientWorld world = client.world;

        if (world != null && client.player != null) {
            Vec3d cameraPos = client.player.getCameraPosVec(tickDelta);
            ChunkSectionVisibilityManager.updateVisibility(world, cameraPos);
        }
    }
}
