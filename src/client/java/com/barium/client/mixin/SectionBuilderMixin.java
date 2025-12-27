package com.barium.client.mixin;

import com.barium.config.BariumConfig;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.chunk.ChunkRendererRegion;
import net.minecraft.client.render.chunk.SectionBuilder;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import com.mojang.blaze3d.systems.VertexSorter;

@Mixin(SectionBuilder.class)
public class SectionBuilderMixin {

    @Unique
    private boolean barium_isDistant = false;

    // Detecta se a seção está longe antes de começar a construir
    @Inject(method = "build", at = @At("HEAD"))
    private void barium$checkDistance(ChunkSectionPos sectionPos, ChunkRendererRegion region, VertexSorter sorter, net.minecraft.client.render.chunk.BlockBufferAllocatorStorage allocator, CallbackInfoReturnable<?> cir) {
        if (MinecraftClient.getInstance().player == null) return;
        
        // Calcula a distância manhattan aproximada para velocidade
        BlockPos playerPos = MinecraftClient.getInstance().player.getBlockPos();
        int dx = Math.abs(sectionPos.getMinX() - playerPos.getX());
        int dz = Math.abs(sectionPos.getMinZ() - playerPos.getZ());
        
        // Define o estado de "distante" baseado na config (padrão 32 blocos = 2 chunks)
        int threshold = BariumConfig.C.DISTANT_GEOMETRY_CULL_DISTANCE;
        this.barium_isDistant = (dx > threshold || dz > threshold);
    }

    /**
     * OTIMIZAÇÃO 1: Desativa a ordenação de vértices translúcidos (água/vidro) se estiver longe.
     * Isso reduz o custo de preparação da GPU e CPU, diminuindo o overhead do drawIndexed.
     */
    @ModifyVariable(method = "build", at = @At("HEAD"), argsOnly = true)
    private VertexSorter barium$disableSortingForDistantChunks(VertexSorter sorter) {
        if (BariumConfig.C.DISABLE_DISTANT_TRANSLUCENCY_SORTING && this.barium_isDistant) {
            return null; // VertexSorter nulo = sem ordenação (Renderização muito mais rápida)
        }
        return sorter;
    }

    // Mantém a otimização de seções vazias que já fizemos
    @Inject(method = "build", at = @At("HEAD"), cancellable = true)
    private void barium$cullEmptySections(ChunkSectionPos sectionPos, ChunkRendererRegion renderRegion, VertexSorter vertexSorter, net.minecraft.client.render.chunk.BlockBufferAllocatorStorage allocatorStorage, CallbackInfoReturnable<SectionBuilder.RenderData> cir) {
        if (!BariumConfig.C.ENABLE_EMPTY_CHUNK_SECTION_CULLING) return;
        if (renderRegion == null || isSectionEmpty(renderRegion, sectionPos)) {
            cir.setReturnValue(new SectionBuilder.RenderData());
        }
    }

    @Unique
    private boolean isSectionEmpty(ChunkRendererRegion region, ChunkSectionPos sectionPos) {
        BlockPos.Mutable mutablePos = new BlockPos.Mutable();
        int startX = sectionPos.getMinX();
        int startY = sectionPos.getMinY();
        int startZ = sectionPos.getMinZ();

        for (int x = 0; x < 16; x++) {
            for (int y = 0; y < 16; y++) {
                for (int z = 0; z < 16; z++) {
                    mutablePos.set(startX + x, startY + y, startZ + z);
                    BlockState state = region.getBlockState(mutablePos);
                    
                    if (!state.isAir()) {
                        // OTIMIZAÇÃO 2: Geometry LOD
                        // Se for um bloco distante e "inútil" (detalhe), fingimos que é ar para não renderizar.
                        if (BariumConfig.C.ENABLE_DISTANT_GEOMETRY_CULLING && this.barium_isDistant) {
                            if (isDetailBlock(state.getBlock())) {
                                continue; // Ignora este bloco, trata como ar
                            }
                        }
                        return false; // É um bloco sólido ou importante, renderiza a seção.
                    }
                }
            }
        }
        return true;
    }
    
    @Unique
    private boolean isDetailBlock(Block block) {
        // Lista de blocos que geram geometria complexa (modelos de cruz) e pesam no drawIndexed
        return block == Blocks.SHORT_GRASS || 
               block == Blocks.TALL_GRASS || 
               block == Blocks.FERN || 
               block == Blocks.LARGE_FERN ||
               block instanceof net.minecraft.block.FlowerBlock;
    }
}