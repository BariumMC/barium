package com.barium.client.render;

import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferAllocator;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BuiltBuffer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockRenderView;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class ChunkMesher {

    public record Result(Map<RenderLayer, BuiltBuffer> layerBuffers) {
        public boolean isEmpty() { return this.layerBuffers.isEmpty(); }
    }

    public Result mesh(BlockRenderView world, BlockPos sectionOrigin) {
        BlockRenderManager blockRenderManager = MinecraftClient.getInstance().getBlockRenderManager();
        Map<RenderLayer, BufferBuilder> builders = new EnumMap<>(RenderLayer.class);
        Random random = new Random();
        MatrixStack matrices = new MatrixStack();

        for (int y = 0; y < 16; ++y) {
            for (int z = 0; z < 16; ++z) {
                for (int x = 0; x < 16; ++x) {
                    BlockPos blockPos = sectionOrigin.add(x, y, z);
                    BlockState state = world.getBlockState(blockPos);

                    if (state.isAir()) continue;

                    RenderLayer layer = RenderLayers.getBlockLayer(state);
                    BufferBuilder builder = builders.computeIfAbsent(layer, l -> {
                        BufferBuilder newBuilder = new BufferBuilder(new BufferAllocator(), l.getDrawMode(), l.getVertexFormat());
                        return newBuilder;
                    });
                    
                    matrices.push();
                    matrices.translate(x, y, z);
                    
                    // A API correta é render "baked model" no buffer
                    blockRenderManager.getModelRenderer().render(matrices.peek(), builder, state, blockRenderManager.getModel(state), 1.0f, 1.0f, 1.0f, 0, 0);

                    matrices.pop();
                }
            }
        }
        
        Map<RenderLayer, BuiltBuffer> builtBuffers = new EnumMap<>(RenderLayer.class);
        builders.forEach((layer, builder) -> {
            BuiltBuffer built = builder.end();
            if (built != null) {
                builtBuffers.put(layer, built);
            }
        });

        return new Result(builtBuffers);
    }
}