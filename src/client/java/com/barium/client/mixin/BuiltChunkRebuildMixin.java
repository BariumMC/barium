package com.barium.client.mixin;

import com.barium.client.optimization.ChunkRebuildOptimizer;
import net.minecraft.client.render.chunk.ChunkBuilder;
import net.minecraft.world.chunk.ChunkSection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

// O alvo é a classe interna BuiltChunk, dentro de ChunkBuilder
@Mixin(ChunkBuilder.BuiltChunk.class)
public class BuiltChunkRebuildMixin {

    /**
     * Redireciona a verificação `section.isEmpty()` dentro do método `rebuild`.
     * Isso nos permite substituir a verificação padrão do Minecraft pela nossa,
     * que é mais completa e agressiva no culling de seções vazias.
     *
     * @param section A ChunkSection sendo verificada.
     * @return true se a seção deve ser pulada (considerada vazia pela nossa lógica), false caso contrário.
     */
    @Redirect(
        method = "rebuild(Lnet/minecraft/client/render/chunk/ChunkBuilder$ChunkData;)Ljava/util/Set;",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/chunk/ChunkSection;isEmpty()Z")
    )
    private boolean barium$cullEmptyChunkSections(ChunkSection section) {
        // Chamamos nossa classe de otimização para decidir se esta seção deve ser pulada.
        return ChunkRebuildOptimizer.shouldSkipSection(section);
    }
}