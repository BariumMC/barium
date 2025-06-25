package com.barium.client.mixin;

import com.barium.client.optimization.ChunkRebuildOptimizer;
import net.minecraft.client.render.chunk.ChunkBuilder;
import net.minecraft.world.chunk.ChunkSection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Mixin que mira na classe interna BuiltChunk para otimizar a reconstrução.
 * Este é o local correto para a otimização de pular seções vazias.
 */
@Mixin(ChunkBuilder.BuiltChunk.class)
public class BuiltChunkRebuildMixin {

    /**
     * Redireciona a chamada a 'section.isEmpty()' dentro do método 'rebuild'
     * da classe BuiltChunk. Esta é a implementação correta e final.
     *
     * @param section A ChunkSection que está sendo verificada.
     * @return true se a seção deve ser pulada, false caso contrário.
     */
    @Redirect(
        // Usando o seletor exato que você forneceu, que aponta para o método 'rebuild'
        // dentro da classe interna 'BuiltChunk'.
        method = "rebuild(Lnet/minecraft/client/render/chunk/ChunkRendererRegionBuilder;)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/chunk/ChunkSection;isEmpty()Z")
    )
    private boolean barium$cullEmptyChunkSections(ChunkSection section) {
        return ChunkRebuildOptimizer.shouldSkipSection(section);
    }
}