package com.barium.client.mixin;

import com.barium.client.util.ChunkVisibilityManager;
import com.barium.config.BariumConfig;
import net.minecraft.client.render.chunk.ChunkBuilder;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ChunkBuilder.BuiltChunk.class)
public abstract class BuiltChunkRebuildMixin {

    /**
     * CORREÇÃO: A injeção foi trocada para @ModifyVariable, que é mais estável.
     * Ela intercepta a variável 'chunkSection' logo após ser carregada.
     * Isso nos permite substituí-la por 'null' se ela não for visível,
     * fazendo o jogo pular sua reconstrução de forma eficiente e segura.
     */
    @ModifyVariable(
            method = "rebuild(Lnet/minecraft/client/render/chunk/ChunkBuilder$RebuildTask;)Ljava/util/Set;",
            at = @At(value = "STORE", ordinal = 0),
            // ordinal = 0 garante que estamos pegando a primeira variável do tipo ChunkSection
            ordinal = 0
    )
    private @Nullable ChunkSection barium$cullUnseenSections(ChunkSection originalSection, ChunkBuilder.RebuildTask task) {
        // Se a otimização estiver desligada, ou se a seção já for nula/vazia, não fazemos nada.
        if (!BariumConfig.C.ENABLE_ADVANCED_SECTION_CULLING || originalSection == null || originalSection.isEmpty()) {
            return originalSection;
        }

        Chunk chunk = task.getChunk();
        int sectionIndex = task.getSectionIndex();

        // CORREÇÃO: O método chunk.getSectionY() foi removido. Usamos a conversão de coordenadas.
        // A posição da seção é seu X e Z em coordenadas de chunk e seu Y em coordenadas de seção.
        int sectionX = chunk.getPos().x;
        int sectionY = chunk.getPos().getSectionY(sectionIndex);
        int sectionZ = chunk.getPos().z;

        // Pergunta ao nosso manager se esta seção específica é visível.
        if (ChunkVisibilityManager.getInstance().isSectionPotentiallyVisible(sectionX, sectionY, sectionZ)) {
            return originalSection; // Se for visível, retorna a seção para ser renderizada.
        } else {
            return null; // Se NÃO for visível, retorna null. O jogo vai tratar como se fosse ar.
        }
    }
}