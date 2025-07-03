package com.barium.client.mixin;

import com.barium.client.util.ChunkCullingUtils;
import com.barium.client.util.ChunkRenderManager;
import com.barium.client.util.FloodFillVisibilityManager;
import com.barium.config.BariumConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.chunk.ChunkBuilder;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChunkBuilder.BuiltChunk.class)
public abstract class ChunkRenderMixin {

    @Shadow public abstract BlockPos getOrigin();

    @Inject(method = "shouldBuild()Z", at = @At("HEAD"), cancellable = true)
    private void barium$onShouldBuild(CallbackInfoReturnable<Boolean> cir) {
        BlockPos origin = this.getOrigin();
        
        // --- Otimização de Oclusão Total (Ideal para subsolo) ---
        if (BariumConfig.C.ENABLE_OCCLUSION_CULLING) {
            if (ChunkCullingUtils.isSectionTotallyOccluded(MinecraftClient.getInstance().world, origin)) {
                cir.setReturnValue(false);
                return;
            }
        }
        
        // --- Otimização de Visibilidade por Flood-Fill ---
        if (BariumConfig.C.ENABLE_FLOOD_FILL_CULLING) {
            int sectionX = origin.getX() >> 4;
            int sectionY = origin.getY() >> 4;
            int sectionZ = origin.getZ() >> 4;
            if (!FloodFillVisibilityManager.getInstance().isSectionVisible(sectionX, sectionY, sectionZ)) {
                cir.setReturnValue(false);
                return;
            }
        }

        // --- Otimização de Frustum Culling (Sua implementação existente) ---
        if (BariumConfig.C.ENABLE_FRUSTUM_CHUNK_CULLING) {
            final int chunkX = origin.getX() >> 4;
            final int chunkZ = origin.getZ() >> 4;
            if (!ChunkRenderManager.getInstance().isChunkInFrustum(chunkX, chunkZ)) {
                cir.setReturnValue(false);
                return;
            }
        }
    }
}