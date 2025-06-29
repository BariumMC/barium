package com.barium.client.mixin;

import com.barium.client.util.ChunkSectionUtils;
import com.barium.client.util.ChunkVisibilityManager;
import com.barium.config.BariumConfig;
import net.minecraft.client.render.chunk.ChunkBuilder;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ChunkBuilder.BuiltChunk.class)
public abstract class BuiltChunkRebuildMixin {

    /**
     * Esta é a injeção da Otimização Avançada de Seções.
     * Nós redirecionamos a chamada que pega uma seção de chunk para ser reconstruída.
     * Antes de entregá-la ao Minecraft, verificamos com nosso ChunkVisibilityManager.
     * Se a seção não for visível, nós retornamos 'null' (como se ela estivesse vazia).
     * O jogo então pula completamente a reconstrução dessa seção, economizando muita CPU.
     */
    @Redirect(
        method = "rebuild(Lnet/minecraft/client/render/chunk/ChunkBuilder$RebuildTask;)Ljava/util/Set;",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/chunk/Chunk;getSection(I)Lnet/minecraft/world/chunk/ChunkSection;")
    )
    private ChunkSection barium$cullUnseenSections(Chunk chunk, int sectionIndex) {
        // Se a otimização estiver desligada, retorna a seção original sem fazer nada.
        if (!BariumConfig.C.ENABLE_ADVANCED_SECTION_CULLING) {
            return chunk.getSection(sectionIndex);
        }
        
        ChunkSection originalSection = chunk.getSection(sectionIndex);

        // Se a seção já for considerada vazia pela lógica vanilla, não precisamos fazer nada.
        if (ChunkSectionUtils.isSectionEmpty(originalSection)) {
            return originalSection;
        }

        // Pega a posição global da seção no mundo.
        int sectionX = chunk.getPos().x;
        int sectionY = chunk.getSectionY(sectionIndex);
        int sectionZ = chunk.getPos().z;

        // Pergunta ao nosso manager se esta seção específica é visível.
        if (ChunkVisibilityManager.getInstance().isSectionPotentiallyVisible(sectionX, sectionY, sectionZ)) {
            // Se for visível, retorna a seção original para ser renderizada.
            return originalSection;
        } else {
            // Se NÃO for visível, retorna null. O jogo vai tratar como se fosse uma seção de ar.
            return null;
        }
    }
}