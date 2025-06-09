package com.barium.client.mixin;

import com.barium.client.util.ChunkSectionUtils;
import com.barium.config.BariumConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.chunk.ChunkBuilder;
import net.minecraft.client.render.chunk.ChunkBuilder.BuiltChunk;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.CompletableFuture;

@Mixin(ChunkBuilder.class)
public abstract class ChunkBuilderMixin {

    @Inject(
        method = "rebuild",
        at = @At("HEAD"),
        cancellable = true
    )
    private void barium$preventEmptyChunkRebuild(BuiltChunk chunk, CallbackInfoReturnable<CompletableFuture<?>> cir) {
        if (!BariumConfig.ENABLE_EMPTY_CHUNK_CULLING) {
            return;
        }

        // CORREÇÃO: Obtemos o 'world' diretamente do cliente. É mais seguro e evita o erro do @Shadow.
        World world = MinecraftClient.getInstance().world;
        if (world == null) {
            return;
        }

        BlockPos origin = chunk.getOrigin();
        int sectionY = world.getSectionIndex(origin.getY());

        if (ChunkSectionUtils.isSectionEmpty(world.getChunk(origin).getSection(sectionY))) {
            // CORREÇÃO: Retornamos um futuro nulo para pular a reconstrução.
            cir.setReturnValue(CompletableFuture.completedFuture(null));
        }
    }
}