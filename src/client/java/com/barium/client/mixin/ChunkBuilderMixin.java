package com.barium.client.mixin;

import com.barium.client.util.ChunkSectionUtils;
import com.barium.config.BariumConfig;
import net.minecraft.client.render.chunk.ChunkBuilder;
import net.minecraft.client.render.chunk.ChunkBuilder.BuiltChunk;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.CompletableFuture;

@Mixin(ChunkBuilder.class)
public abstract class ChunkBuilderMixin {

    @Shadow
    private World world;

    @Inject(
        // SUA CORREÇÃO APLICADA: Usando apenas o nome do método para maior robustez.
        method = "rebuild",
        at = @At("HEAD"),
        cancellable = true
    )
    private void barium$preventEmptyChunkRebuild(BuiltChunk chunk, CallbackInfoReturnable<CompletableFuture<ChunkBuilder.Result>> cir) {
        if (!BariumConfig.ENABLE_EMPTY_CHUNK_CULLING) {
            return;
        }

        if (this.world == null) {
            return;
        }

        BlockPos origin = chunk.getOrigin();
        int sectionY = this.world.getSectionIndex(origin.getY());

        if (ChunkSectionUtils.isSectionEmpty(this.world.getChunk(origin).getSection(sectionY))) {
            cir.setReturnValue(CompletableFuture.completedFuture(null));
        }
    }
}