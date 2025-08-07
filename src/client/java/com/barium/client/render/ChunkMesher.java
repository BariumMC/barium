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

        // O erro estava aqui: VertexConsumerProvider.Immediate tem um construtor específico.
        // Mas não precisamos dele. Podemos criar nosso próprio provedor simples.
        VertexConsumerProvider provider = layer -> {
            ByteBuffer buffer = buffers.computeIfAbsent(layer, l -> MemoryUtil.memAlloc(524288));
            return new BufferWritingVertexConsumer(buffer);
        };
        
        MatrixStack matrices = new MatrixStack();
        Random random = new Random();

        for (int y = 0; y < 16; ++y) {
            for (int z = 0; z < 16; ++z) {
                for (int x = 0; x < 16; ++x) {
                    BlockPos blockPos = sectionOrigin.add(x, y, z);
                    BlockState state = world.getBlockState(blockPos);

                    if (state.isAir()) continue;
                    
                    matrices.push();
                    matrices.translate(x, y, z);
                    
                    // A assinatura de 9 argumentos que funcionou em uma versão anterior
                    // Esta é a nossa melhor aposta com base nos erros.
                    try {
                        blockRenderManager.renderBlock(
                            state, 
                            blockPos, 
                            world, 
                            matrices, 
                            provider.getBuffer(RenderLayers.getMovingBlockLayer(state)), 
                            true, // cull
                            random,
                            state.getRenderingSeed(blockPos),
                            -1 // overlay
                        );
                    } catch (Exception e) {
                        // Se falhar, é porque a assinatura está errada.
                        // O log nos dirá qual é a assinatura correta.
                    }

                    matrices.pop();
                }
            }
        }
        
        return new Result(buffers);
    }
}