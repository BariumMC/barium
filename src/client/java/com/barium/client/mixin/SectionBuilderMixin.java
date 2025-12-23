// CONTEÚDO CORRIGIDO: src/client/java/com/barium/client/mixin/SectionBuilderMixin.java
package com.barium.client.mixin;

import com.barium.config.BariumConfig;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.chunk.ChunkRendererRegion;
import net.minecraft.client.render.chunk.SectionBuilder;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SectionBuilder.class)
public class SectionBuilderMixin {

    /**
     * Injeta no início do método `build` para pular a renderização de seções de chunk que contêm apenas ar.
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

        // Se a otimização estiver desligada, não fazemos nada.
        if (!BariumConfig.C.ENABLE_EMPTY_CHUNK_SECTION_CULLING) {
            return;
        }

        // Se a região de renderização for nula, saímos para evitar erros.
        if (renderRegion == null) {
            return;
        }

        // CORREÇÃO: Usamos um método auxiliar que inspeciona a ChunkRendererRegion diretamente.
        if (isSectionEmpty(renderRegion, sectionPos)) {
            // Se a seção estiver vazia, cancelamos o método original e retornamos dados de renderização vazios.
            cir.setReturnValue(new SectionBuilder.RenderData());
            return;
        }

        // Se ativado, detecta se a seção contém majoritariamente folhas (árvores/florestas)
        // e cancela a construção para reduzir o custo de renderização em biomas densos.
        if (com.barium.config.BariumConfig.C.ENABLE_FOREST_SECTION_CULLING && isSectionMostlyLeaves(renderRegion, sectionPos)) {
            cir.setReturnValue(new SectionBuilder.RenderData());
            return;
        }
    }

    /**
     * Verifica de forma otimizada se uma seção dentro de uma ChunkRendererRegion contém apenas blocos de ar.
     * Esta é a abordagem correta, pois ChunkRendererRegion não expõe a ChunkSection diretamente.
     * @param region A região de renderização fornecida para a construção do chunk.
     * @param sectionPos A posição da seção que estamos verificando.
     * @return true se todos os blocos na seção forem ar, false caso contrário.
     */
    @Unique
    private boolean isSectionEmpty(ChunkRendererRegion region, ChunkSectionPos sectionPos) {
        BlockPos.Mutable mutablePos = new BlockPos.Mutable();
        int startX = sectionPos.getMinX();
        int startY = sectionPos.getMinY();
        int startZ = sectionPos.getMinZ();

        // Itera sobre todos os 4096 blocos da seção (16x16x16).
        for (int y = 0; y < 16; y++) {
            for (int z = 0; z < 16; z++) {
                for (int x = 0; x < 16; x++) {
                    mutablePos.set(startX + x, startY + y, startZ + z);
                    BlockState state = region.getBlockState(mutablePos);
                    // Se encontrarmos qualquer bloco que não seja ar, a seção não está vazia.
                    if (!state.isAir()) {
                        return false;
                    }
                }
            }
        }

        // Se o loop terminar, a seção está completamente vazia.
        return true;
    }

    @org.spongepowered.asm.mixin.Unique
    private boolean isSectionMostlyLeaves(ChunkRendererRegion region, ChunkSectionPos sectionPos) {
        // Para desempenho, amostramos os blocos a cada passo (não verificamos todos os 4096).
        int step = 2; // amostra 1/8 do total aproximadamente
        int leafCount = 0;
        int sampleCount = 0;
        double threshold = com.barium.config.BariumConfig.C.FOREST_SECTION_LEAF_THRESHOLD;

        BlockPos.Mutable mutablePos = new BlockPos.Mutable();
        int startX = sectionPos.getMinX();
        int startY = sectionPos.getMinY();
        int startZ = sectionPos.getMinZ();

        for (int y = 0; y < 16; y += step) {
            for (int z = 0; z < 16; z += step) {
                for (int x = 0; x < 16; x += step) {
                    mutablePos.set(startX + x, startY + y, startZ + z);
                    net.minecraft.block.BlockState state = region.getBlockState(mutablePos);
                    sampleCount++;
                    // Usa uma verificação direta por blocos de folha (evita dependência em tags que podem mudar).
                    if (isLeafBlock(state)) {
                        leafCount++;
                    }
                }
            }
        }

        if (sampleCount == 0) return false;

        double ratio = (double) leafCount / (double) sampleCount;
        return ratio >= threshold;
    }

    @org.spongepowered.asm.mixin.Unique
    private boolean isLeafBlock(net.minecraft.block.BlockState state) {
        net.minecraft.block.Block block = state.getBlock();
        return block == net.minecraft.block.Blocks.OAK_LEAVES
                || block == net.minecraft.block.Blocks.SPRUCE_LEAVES
                || block == net.minecraft.block.Blocks.BIRCH_LEAVES
                || block == net.minecraft.block.Blocks.JUNGLE_LEAVES
                || block == net.minecraft.block.Blocks.ACACIA_LEAVES
                || block == net.minecraft.block.Blocks.DARK_OAK_LEAVES
                || block == net.minecraft.block.Blocks.MANGROVE_LEAVES
                || block == net.minecraft.block.Blocks.AZALEA_LEAVES
                || block == net.minecraft.block.Blocks.FLOWERING_AZALEA_LEAVES;
    }
}