package com.barium.client.render;

import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
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
    
    private final Random random = Random.create();

    public Result mesh(BlockRenderView world, BlockPos sectionOrigin) {
        BlockRenderManager blockRenderManager = MinecraftClient.getInstance().getBlockRenderManager();
        Map<RenderLayer, BufferWritingVertexConsumer> consumerMap = new ConcurrentHashMap<>();
        Map<RenderLayer, ByteBuffer> buffers = new ConcurrentHashMap<>();

        VertexConsumerProvider provider = layer -> consumerMap.computeIfAbsent(layer, l -> {
            ByteBuffer buffer = MemoryUtil.memAlloc(1024 * 512);
            buffers.put(l, buffer);
            return new BufferWritingVertexConsumer(buffer);
        });
        
        MatrixStack matrices = new MatrixStack();

        for (int y = 0; y < 16; ++y) {
            for (int z = 0; z < 16; ++z) {
                for (int x = 0; x < 16; ++x) {
                    BlockPos blockPos = sectionOrigin.add(x, y, z);
                    BlockState state = world.getBlockState(blockPos);

                    // CORREÇÃO 1: O método isOpaqueFullCube() não recebe argumentos.
                    if (state.isOpaqueFullCube()) continue;
                    
                    matrices.push();
                    matrices.translate(x, y, z);
                    
                    RenderLayer layer = RenderLayers.getMovingBlockLayer(state);
                    // CORREÇÃO 2: O método renderBlock espera uma List, não um Random.
                    blockRenderManager.renderBlock(state, blockPos, world, matrices, provider.getBuffer(layer), true, Collections.emptyList());

                    matrices.pop();
                }
            }
        }
        
        buffers.forEach((layer, buffer) -> {
            BufferWritingVertexConsumer consumer = consumerMap.get(layer);
            if(consumer != null) {
                buffer.limit(consumer.getBytesWritten());
            }
        });

        return new Result(buffers);
    }
}