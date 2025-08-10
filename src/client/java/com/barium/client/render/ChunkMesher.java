package com.barium.client.render;

import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockRenderView;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.Random;
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
    
    private static final int VANILLA_STRIDE_INTS = 8;

    public Result mesh(BlockRenderView world, BlockPos sectionOrigin) {
        BlockRenderManager blockRenderManager = MinecraftClient.getInstance().getBlockRenderManager();
        BlockColors blockColors = MinecraftClient.getInstance().getBlockColors();
        Map<RenderLayer, ByteBuffer> buffers = new ConcurrentHashMap<>();
        Random random = new Random();

        for (int y = 0; y < 16; ++y) {
            for (int z = 0; z < 16; ++z) {
                for (int x = 0; x < 16; ++x) {
                    BlockPos blockPos = sectionOrigin.add(x, y, z);
                    BlockState state = world.getBlockState(blockPos);

                    if (state.isAir()) continue;

                    MatrixStack matrixStack = new MatrixStack();
                    BakedModel model = blockRenderManager.getModel(state);
                    
                    for (Direction dir : Direction.values()) {
                        random.setSeed(state.getRenderingSeed(blockPos));
                        writeQuads(world, state, blockPos, matrixStack, dir, model.getQuads(state, dir, random), buffers, blockColors);
                    }
                    random.setSeed(state.getRenderingSeed(blockPos));
                    writeQuads(world, state, blockPos, matrixStack, null, model.getQuads(state, null, random), buffers, blockColors);
                }
            }
        }
        return new Result(buffers);
    }

    private void writeQuads(BlockRenderView world, BlockState state, BlockPos pos, MatrixStack matrixStack, Direction dir, List<BakedQuad> quads, Map<RenderLayer, ByteBuffer> buffers, BlockColors blockColors) {
        if (quads.isEmpty()) return;

        RenderLayer layer = RenderLayers.getMovingBlockLayer(state);
        ByteBuffer buffer = buffers.computeIfAbsent(layer, l -> MemoryUtil.memAlloc(524288));
        BufferWritingVertexConsumer consumer = new BufferWritingVertexConsumer(buffer);

        int packedColor = -1;
        if (quads.get(0).hasColor()) {
            packedColor = blockColors.getColor(state, world, pos, quads.get(0).getColorIndex());
        }

        for (BakedQuad quad : quads) {
            consumer.quad(matrixStack.peek(), quad, packedColor, 1.0f, 1.0f, 1.0f, 0, 0, true);
        }
    }
}