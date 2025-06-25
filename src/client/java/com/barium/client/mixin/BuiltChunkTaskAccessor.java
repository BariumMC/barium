package com.barium.client.mixin;

import net.minecraft.client.render.chunk.ChunkBuilder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Este Mixin Accessor nos dá acesso ao campo privado 'chunk'
 * dentro da classe interna ChunkBuilder.BuiltChunk.Task.
 */
@Mixin(ChunkBuilder.BuiltChunk.Task.class)
public interface BuiltChunkTaskAccessor {

    /**
     * Gera um método que nos permite ler o valor do campo 'chunk'.
     * O nome do campo pode variar entre versões, mas geralmente é 'chunk' ou 'field_...'.
     * Se 'chunk' não funcionar, teremos que verificar os mapeamentos.
     * @return O BuiltChunk associado a esta tarefa.
     */
    @Accessor("chunk")
    ChunkBuilder.BuiltChunk getChunk();
}