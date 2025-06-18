package com.barium.client.mixin;

import com.barium.client.optimization.ChunkRebuildOptimizer;
import net.minecraft.client.render.chunk.ChunkBuilder;
import net.minecraft.world.chunk.ChunkSection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ChunkBuilder.BuiltChunk.class)
public class BuiltChunkRebuildMixin {

    // Esta assinatura é um ponto comum de falha entre versões. VERIFIQUE-A!
    @Redirect(
        method = "rebuild(Lnet/minecraft/client/render/chunk/ChunkBuilder$RebuildTask;)Ljava/util/Set;",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/chunk/ChunkSection;isEmpty()Z"),
        remap = false // Adicione remap=false como teste se os mapeamentos estiverem falhando.
    )
    private boolean barium$cullEmptyChunkSections(ChunkSection section) {
        return ChunkRebuildOptimizer.shouldSkipSection(section);
    }
}