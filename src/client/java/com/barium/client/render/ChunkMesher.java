package com.barium.client.render;

import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
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
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class ChunkMesher {

    // A CORREÇÃO ESTÁ AQUI
    public record Result(Map<RenderLayer, ByteBuffer> layerBuffers) {
        /**
         * Verifica se o resultado do meshing está vazio (nenhum vértice foi gerado).
         * @return true se nenhum layer tiver um buffer.
         */
        public boolean isEmpty() {
            return this.layerBuffers.isEmpty();
        }

        /**
         * Libera a memória de todos os ByteBuffers alocados.
         */
        public void free() {
            if (this.layerBuffers != null) {
                this.layerBuffers.values().forEach(MemoryUtil::memFree);
            }
        }
    }

    private final Random random = new Random();

    public Result mesh(BlockRenderView world, BlockPos sectionOrigin) {
        BlockRenderManager blockRenderManager = MinecraftClient.getInstance().getBlockRenderManager();
        // Usamos EnumMap para performance, já que as chaves são RenderLayers.
        Map<RenderLayer, ByteBuffer> buffers = new EnumMap<>(RenderLayer.class);

        BlockPos.Mutable mutablePos = new BlockPos.Mutable();

        for (int y = 0; y < 16; ++y) {
            for (int z = 0; z < 16; ++z) {
                for (int x = 0; x < 16; ++x) {
                    mutablePos.set(sectionOrigin.getX() + x, sectionOrigin.getY() + y, sectionOrigin.getZ() + z);
                    BlockState state = world.getBlockState(mutablePos);

                    if (state.isAir()) {
                        continue;
                    }

                    BakedModel model = blockRenderManager.getModel(state);
                    random.setSeed(state.getRenderingSeed(mutablePos));

                    // Itera por todas as 7 "direções" (6 faces + null para quads gerais)
                    for (Direction dir : Direction.values()) {
                        processQuads(world, state, mutablePos, dir, model.getQuads(state, dir, random), buffers);
                    }
                    processQuads(world, state, mutablePos, null, model.getQuads(state, null, random), buffers);

                }
            }
        }
        
        return new Result(buffers);
    }
    
    private void processQuads(BlockRenderView world, BlockState state, BlockPos pos, Direction dir, List<BakedQuad> quads, Map<RenderLayer, ByteBuffer> buffers) {
        if (quads.isEmpty()) {
            return;
        }

        // TODO: Sua lógica de culling de face vai aqui. Se a face não deve ser renderizada, retorne.
        // Ex: if (dir != null && !Block.shouldRenderFace(state, world, pos, dir)) return;

        RenderLayer layer = RenderLayers.getMovingBlockLayer(state);
        // O ByteBuffer é alocado sob demanda quando o primeiro quad para um layer é encontrado.
        ByteBuffer buffer = buffers.computeIfAbsent(layer, l -> MemoryUtil.memAlloc(262144)); // 256 KB por buffer de layer

        for (BakedQuad quad : quads) {
            // TODO: Chamar o método writeQuad aqui
            // writeQuad(buffer, quad, ...);
        }
    }

    private void writeQuad(ByteBuffer buffer, BakedQuad quad, float r, float g, float b, int light, int overlay) {
        // TODO: Implementar a escrita dos vértices.
    }
}