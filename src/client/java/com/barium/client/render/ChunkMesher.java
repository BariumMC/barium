package com.barium.client.render;

import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.BlockRenderManager;
// CORREÇÃO: O caminho do import foi corrigido.
import net.minecraft.client.render.model.BlockModelPart; 
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class ChunkMesher {

    public record Result(Map<RenderLayer, ByteBuffer> layerBuffers) {
        public boolean isEmpty() {
            return this.layerBuffers.isEmpty();
        }
        public void free() {
            if (this.layerBuffers != null) {
                this.layerBuffers.values().forEach(MemoryUtil::memFree);
            }
        }
    }

    public Result mesh(BlockRenderView world, BlockPos sectionOrigin) {
        BlockRenderManager blockRenderManager = MinecraftClient.getInstance().getBlockRenderManager();
        Map<RenderLayer, ByteBuffer> buffers = new ConcurrentHashMap<>();

        VertexConsumerProvider.Immediate provider = new VertexConsumerProvider.Immediate() {
            @Override
            public VertexConsumer getBuffer(RenderLayer layer) {
                ByteBuffer buffer = buffers.computeIfAbsent(layer, l -> MemoryUtil.memAlloc(524288));
                return new BufferWritingVertexConsumer(buffer);
            }
            @Override
            public void draw() { /* Não fazemos nada */ }
            @Override
            public void draw(RenderLayer layer) { /* Não fazemos nada */ }
        };
        
        MatrixStack matrices = new MatrixStack();
        Random random = new Random();

        for (int y = 0; y < 16; ++y) {
            for (int z = 0; z < 16; ++z) {
                for (int x = 0; x < 16; ++x) {
                    BlockPos blockPos = sectionOrigin.add(x, y, z);
                    BlockState state = world.getBlockState(blockPos);

                    if (state.isAir()) {
                        continue;
                    }
                    
                    matrices.push();
                    matrices.translate(x, y, z);
                    
                    // CORREÇÃO FINAL DA ASSINATURA DE renderBlock
                    // A assinatura correta espera uma List<BlockModelPart> como último argumento.
                    // Nós passamos uma lista vazia porque queremos renderizar o bloco inteiro.
                    blockRenderManager.renderBlock(
                        state, 
                        blockPos, 
                        world, 
                        matrices, 
                        provider.getBuffer(RenderLayers.getMovingBlockLayer(state)), 
                        true, // cull
                        // O erro anterior estava aqui. A assinatura correta não usa Random, usa List.
                        Collections.emptyList()
                    );

                    matrices.pop();
                }
            }
        }
        
        return new Result(buffers);
    }
}