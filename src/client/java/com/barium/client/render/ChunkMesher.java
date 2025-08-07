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
import java.util.EnumMap;
import java.util.Map;
import java.util.Random;

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

    private final Random random = new Random();

    public Result mesh(BlockRenderView world, BlockPos sectionOrigin) {
        BlockRenderManager blockRenderManager = MinecraftClient.getInstance().getBlockRenderManager();
        Map<RenderLayer, ByteBuffer> buffers = new EnumMap<>(RenderLayer.class);

        // Criamos um provedor de VertexConsumers que usa nossos buffers.
        VertexConsumerProvider.Immediate provider = layer -> {
            ByteBuffer buffer = buffers.computeIfAbsent(layer, l -> MemoryUtil.memAlloc(524288)); // 512 KB
            return new BufferWritingVertexConsumer(buffer);
        };
        
        MatrixStack matrices = new MatrixStack();

        for (int y = 0; y < 16; ++y) {
            for (int z = 0; z < 16; ++z) {
                for (int x = 0; x < 16; ++x) {
                    BlockPos blockPos = sectionOrigin.add(x, y, z);
                    BlockState state = world.getBlockState(blockPos);

                    if (state.isAir()) {
                        continue;
                    }
                    
                    // Prepara a matriz de transformação para a posição do bloco
                    matrices.push();
                    matrices.translate(x, y, z);
                    
                    // O "TRUQUE": Dizemos ao BlockRenderManager para renderizar o bloco.
                    // Em vez de ir para a tela, ele vai para o nosso BufferWritingVertexConsumer,
                    // que escreve os dados de vértice no nosso ByteBuffer.
                    // O método `renderBlock` foi renomeado e sua assinatura mudou, vamos usar a correta
                    blockRenderManager.renderBlock(state, blockPos, world, matrices, provider.getBuffer(RenderLayers.getMovingBlockLayer(state)), true, random);

                    matrices.pop();
                }
            }
        }
        
        return new Result(buffers);
    }
}