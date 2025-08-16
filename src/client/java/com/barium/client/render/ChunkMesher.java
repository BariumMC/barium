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

    // Adicionado um objeto Random para a chamada de renderBlock
    private final Random random = Random.create();

    public Result mesh(BlockRenderView world, BlockPos sectionOrigin) {
        BlockRenderManager blockRenderManager = MinecraftClient.getInstance().getBlockRenderManager();
        Map<RenderLayer, ByteBuffer> buffers = new ConcurrentHashMap<>();

        VertexConsumerProvider.Immediate provider = VertexConsumerProvider.immediate(new ConcurrentHashMap<RenderLayer, ByteBuffer>() {
            @Override
            public ByteBuffer get(Object layer) {
                return buffers.computeIfAbsent((RenderLayer) layer, l -> MemoryUtil.memAlloc(524288));
            }
        });

        MatrixStack matrices = new MatrixStack();

        for (int y = 0; y < 16; ++y) {
            for (int z = 0; z < 16; ++z) {
                for (int x = 0; x < 16; ++x) {
                    BlockPos blockPos = sectionOrigin.add(x, y, z);
                    BlockState state = world.getBlockState(blockPos);

                    if (state.isAir()) continue;

                    // CORREÇÃO: Obtém a camada de renderização correta para o estado do bloco
                    RenderLayer renderLayer = RenderLayers.getRenderLayer(state);
                    
                    matrices.push();
                    matrices.translate(x, y, z);
                    
                    // CORREÇÃO: Passa o 'random' e usa o buffer da camada correta
                    blockRenderManager.renderBlock(
                        state, 
                        blockPos, 
                        world, 
                        matrices, 
                        provider.getBuffer(renderLayer), 
                        true,
                        this.random
                    );

                    matrices.pop();
                }
            }
        }
        
        provider.draw();
        return new Result(buffers);
    }
}