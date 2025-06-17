package com.barium.client.mixin;

import com.barium.client.optimization.ChunkRebuildOptimizer;
import net.minecraft.client.render.chunk.ChunkBuilder;
import net.minecraft.world.chunk.ChunkSection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Set; // Ensure Set is imported

@Mixin(ChunkBuilder.BuiltChunk.class)
public class BuiltChunkRebuildMixin {

    /**
     * Redireciona a chamada original `section.isEmpty()` para a nossa lógica mais robusta.
     * Esta é a forma mais eficiente de implementar o culling de seções vazias.
     * A assinatura do método alvo está correta para o Minecraft 1.21.6 se o método for este.
     */
    @Redirect(
        method = "rebuild(Lnet/minecraft/client/render/chunk/ChunkBuilder$RebuildTask;)Ljava/util/Set;", // Verify this signature
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/chunk/ChunkSection;isEmpty()Z")
    )
    private boolean barium$cullEmptyChunkSections(ChunkSection section) {
        return ChunkRebuildOptimizer.shouldSkipSection(section);
    }
}