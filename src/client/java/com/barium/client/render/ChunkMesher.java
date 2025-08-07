package com.barium.client.render;

import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.block.BlockRenderManager;
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
        public boolean isEmpty() {
            return this.layerBuffers.isEmpty();
        }
        public void free() {
            if (this.layerBuffers != null) {
                this.layerBuffers.values().forEach(MemoryUtil::memFree);
            }
        }
    }
    
    // Vanilla vertex data stride (em ints)
    private static final int VANILLA_STRIDE = 8;

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

                    // A chamada correta para obter o modelo.
                    BakedModel model = blockRenderManager.getModel(state);
                    
                    // Itera por todas as direções para obter as faces do modelo.
                    for (Direction dir : Direction.values()) {
                        random.setSeed(state.getRenderingSeed(blockPos));
                        writeQuads(world, state, blockPos, dir, model.getQuads(state, dir, random), buffers, blockColors);
                    }
                    // Também processa quads "gerais" que não pertencem a nenhuma face (ex: flores)
                    random.setSeed(state.getRenderingSeed(blockPos));
                    writeQuads(world, state, blockPos, null, model.getQuads(state, null, random), buffers, blockColors);
                }
            }
        }
        
        return new Result(buffers);
    }
    
    private void writeQuads(BlockRenderView world, BlockState state, BlockPos pos, Direction dir, List<BakedQuad> quads, Map<RenderLayer, ByteBuffer> buffers, BlockColors blockColors) {
        if (quads.isEmpty()) return;

        // TODO: Culling de face. if (dir != null && !Block.shouldRenderFace(state, ...)) return;

        RenderLayer layer = RenderLayers.getMovingBlockLayer(state);
        ByteBuffer buffer = buffers.computeIfAbsent(layer, l -> MemoryUtil.memAlloc(524288)); // 512 KB
        BariumVertexFormat.Writer writer = new BariumVertexFormat.Writer(buffer);

        for (BakedQuad quad : quads) {
            int packedColor = -1; // -1 significa branco (sem tint)
            if (quad.hasColor()) {
                // Obtém a cor de "tint" do bloco (ex: grama, folhas)
                packedColor = blockColors.getColor(state, world, pos, quad.getColorIndex());
            }

            // Extrai a cor em componentes float
            float r = ((packedColor >> 16) & 0xFF) / 255.0f;
            float g = ((packedColor >> 8) & 0xFF) / 255.0f;
            float b = (packedColor & 0xFF) / 255.0f;

            int[] vertexData = quad.getVertexData();
            
            // Cada quad tem 4 vértices.
            for (int i = 0; i < 4; i++) {
                int offset = i * VANILLA_STRIDE;

                // Desempacota os dados brutos do array de inteiros
                float vx = Float.intBitsToFloat(vertexData[offset]);
                float vy = Float.intBitsToFloat(vertexData[offset + 1]);
                float vz = Float.intBitsToFloat(vertexData[offset + 2]);
                
                // TODO: A cor dos vértices pode ser usada em vez da cor de tint
                // int color = vertexData[offset + 3];

                float u = Float.intBitsToFloat(vertexData[offset + 4]);
                float v = Float.intBitsToFloat(vertexData[offset + 5]);
                
                int light = vertexData[offset + 6];

                // Escreve os dados no nosso formato de vértice customizado
                writer.writePosNormal(vx, vy, vz, 0); // TODO: Normal
                writer.writeColor(r, g, b, 1.0f);
                writer.writeTexture(u, v);
                writer.writeLight(light, 0); // TODO: Overlay
                writer.next();
            }
        }
    }
}