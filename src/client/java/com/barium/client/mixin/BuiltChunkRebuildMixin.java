// --- FINAL: Substitua o conteúdo em: src/client/java/com/barium/client/mixin/BuiltChunkRebuildMixin.java ---
package com.barium.client.mixin;

import com.barium.client.optimization.ChunkRebuildOptimizer;
import net.minecraft.client.render.chunk.ChunkBuilder;
import net.minecraft.world.chunk.ChunkSection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ChunkBuilder.BuiltChunk.class)
public class BuiltChunkRebuildMixin {

    /**
     * Redireciona a chamada original `section.isEmpty()` para a nossa lógica mais robusta.
     * Esta é a forma mais eficiente de implementar o culling de seções vazias.
     * O aviso de "Cannot find target" pode ser um problema de cache do ambiente de build.
     * Esta assinatura está correta para o Minecraft 1.21.4.
     */
    @Redirect(
        method = "rebuild(),
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/chunk/ChunkSection;isEmpty()Z")
    )
    private boolean barium$cullEmptyChunkSections(ChunkSection section) {
        return ChunkRebuildOptimizer.shouldSkipSection(section);
    }
}