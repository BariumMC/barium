package com.barium.client.render;

import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
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
        MinecraftClient client = MinecraftClient.getInstance();
        BlockRenderManager blockRenderManager = client.getBlockRenderManager();
        BlockColors blockColors = client.getBlockColors();
        Map<RenderLayer, ByteBuffer> buffers = new ConcurrentHashMap<>();
        Random random = new Random();

        for (int y = 0; y < 16; ++y) {
            for (int z = 0; z < 16; ++z) {
                for (int x = 0; x < 16; ++x) {
                    BlockPos blockPos = sectionOrigin.add(x, y, z);
                    BlockState state = world.getBlockState(blockPos);

                    if (state.isAir()) continue;

                    BakedModel model = blockRenderManager.getModel(state);
                    
                    for (Direction dir : Direction.values()) {
                        random.setSeed(state.getRenderingSeed(blockPos));
                        writeQuads(world, state, blockPos, dir, model.getQuads(state, dir, random), buffers, blockColors);
                    }
                    random.setSeed(state.getRenderingSeed(blockPos));
                    writeQuads(world, state, blockPos, null, model.getQuads(state, null, random), buffers, blockColors);
                }
            }
        }
    
        return new Result(buffers);
    }

    private void writeQuads(BlockRenderView world, BlockState state, BlockPos pos, Direction dir, List<BakedQuad> quads, Map<RenderLayer, ByteBuffer> buffers, BlockColors blockColors) {
        if (quads.isEmpty()) return;

        RenderLayer layer = RenderLayers.getMovingBlockLayer(state);
        ByteBuffer buffer = buffers.computeIfAbsent(layer, l -> MemoryUtil.memAlloc(524288));
        BariumVertexFormat.Writer writer = new BariumVertexFormat.Writer(buffer);

        for (BakedQuad quad : quads) {
            int packedColor = -1;
            if (quad.hasColor()) {
                packedColor = blockColors.getColor(state, world, pos, quad.getColorIndex());
            }

            int[] vertexData = quad.getVertexData();
            
            for (int i = 0; i < 4; i++) {
                int offset = i * VANILLA_STRIDE_INTS;

                float vx = Float.intBitsToFloat(vertexData[offset]) + pos.getX();
                float vy = Float.intBitsToFloat(vertexData[offset + 1]) + pos.getY();
                float vz = Float.intBitsToFloat(vertexData[offset + 2]) + pos.getZ();
                
                int color = vertexData[offset + 3];
                if (quad.hasColor()) {
                    float r = ((packedColor >> 16) & 0xFF) / 255.0f;
                    float g = ((packedColor >> 8) & 0xFF) / 255.0f;
                    float b = (packedColor & 0xFF) / 255.0f;
                    
                    float vcr = ((color >> 16) & 0xFF) / 255.0f;
                    float vcg = ((color >> 8) & 0xFF) / 255.0f;
                    float vcb = (color & 0xFF) / 255.0f;
                    
                    int finalR = (int)((r * vcr) * 255);
                    int finalG = (int)((g * vcg) * 255);
                    int finalB = (int)((b * vcb) * 255);
                    color = (color & 0xFF000000) | (finalR << 16) | (finalG << 8) | finalB;
                }

                float u = Float.intBitsToFloat(vertexData[offset + 4]);
                float v = Float.intBitsToFloat(vertexData[offset + 5]);
                int light = vertexData[offset + 6];

                writer.writeVertex(vx, vy, vz, color, u, v, light);
            }
        }
        writer.next();
    }
}