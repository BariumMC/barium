// src/client/java/com/barium/client/render/ChunkMesher.java
package com.barium.client.render;

import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ChunkMesher {

    public record Result(Map<RenderLayer, ByteBuffer> layerBuffers) {
        public boolean isEmpty() { return this.layerBuffers.isEmpty(); }
        public void free() {
            if (this.layerBuffers != null) {
                this.layerBuffers.values().forEach(MemoryUtil::memFree);
            }
        }
    }

    public Result mesh(BlockRenderView world, BlockPos sectionOrigin) {
        BlockRenderManager blockRenderManager = MinecraftClient.getInstance().getBlockRenderManager();
        Map<RenderLayer, ByteBuffer> buffers = new ConcurrentHashMap<>();

        // CORREÇÃO: A criação do VertexConsumerProvider foi revertida para a forma correta
        // que não usa VertexConsumerProvider.immediate, que causava o erro de tipo.
        VertexConsumerProvider provider = layer -> {
            ByteBuffer buffer = buffers.computeIfAbsent(layer, l -> MemoryUtil.memAlloc(524288));
            return new BufferWritingVertexConsumer(buffer);
        };
        
        MatrixStack matrices = new MatrixStack();

        for (int y = 0; y < 16; ++y) {
            for (int z = 0; z < 16; ++z) {
                for (int x = 0; x < 16; ++x) {
                    BlockPos blockPos = sectionOrigin.add(x, y, z);
                    BlockState state = world.getBlockState(blockPos);

                    if (state.isAir()) continue;
                    
                    matrices.push();
                    matrices.translate(x, y, z);
                    
                    // CORREÇÃO: O método correto para obter a camada é 'RenderLayers.getBlockLayer'.
                    // E o último parâmetro de 'renderBlock' deve ser uma lista vazia, não um 'Random'.
                    blockRenderManager.renderBlock(
                        state, 
                        blockPos, 
                        world, 
                        matrices, 
                        provider.getBuffer(RenderLayers.getBlockLayer(state)), 
                        true,
                        Collections.emptyList()
                    );

                    matrices.pop();
                }
            }
        }
        
        return new Result(buffers);
    }
}