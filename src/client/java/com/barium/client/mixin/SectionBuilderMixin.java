// CONTEÚDO CORRIGIDO: src/client/java/com/barium/client/mixin/SectionBuilderMixin.java
package com.barium.client.mixin;

import com.barium.client.optimization.ChunkRebuildOptimizer;
import net.minecraft.client.render.chunk.ChunkRendererRegion;
import net.minecraft.client.render.chunk.SectionBuilder;
import net.minecraft.util.math.ChunkSectionPos; // Import adicionado para clareza
import net.minecraft.world.chunk.ChunkSection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SectionBuilder.class)
public class SectionBuilderMixin {

    /**
     * Injeta no início do método `build` da nova classe SectionBuilder.
     * Esta é a localização correta para a otimização de "culling de seções vazias" no Minecraft 1.21.9+.
     * Se a seção do chunk for vazia, cancelamos o método e retornamos um RenderData vazio,
     * economizando todo o trabalho de processamento de blocos.
     */
    @Inject(
        method = "build(Lnet/minecraft/util/math/ChunkSectionPos;Lnet/minecraft/client/render/chunk/ChunkRendererRegion;Lcom/mojang/blaze3d/systems/VertexSorter;Lnet/minecraft/client/render/chunk/BlockBufferAllocatorStorage;)Lnet/minecraft/client/render/chunk/SectionBuilder$RenderData;",
        at = @At("HEAD"),
        cancellable = true
    )
    private void barium$cullEmptySections(
            ChunkSectionPos sectionPos,
            ChunkRendererRegion renderRegion,
            com.mojang.blaze3d.systems.VertexSorter vertexSorter,
            net.minecraft.client.render.chunk.BlockBufferAllocatorStorage allocatorStorage,
            CallbackInfoReturnable<SectionBuilder.RenderData> cir) {

        if (renderRegion == null) {
            return;
        }

        // CORREÇÃO: O método toBlockPos() foi substituído por getMinPos().
        ChunkSection section = renderRegion.getChunkSection(sectionPos.getMinPos());

        if (ChunkRebuildOptimizer.shouldSkipSection(section)) {
            cir.setReturnValue(new SectionBuilder.RenderData());
        }
    }
}