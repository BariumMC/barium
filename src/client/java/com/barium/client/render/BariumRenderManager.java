package com.barium.client.render;

import com.barium.BariumMod;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class BariumRenderManager {
    private static final BariumRenderManager INSTANCE = new BariumRenderManager();
    public static BariumRenderManager getInstance() { return INSTANCE; }

    private static final List<RenderLayer> CHUNK_LAYERS = List.of(
        RenderLayer.getSolid(), 
        RenderLayer.getCutoutMipped(), 
        RenderLayer.getCutout(), 
        RenderLayer.getTranslucentMovingBlock()
    );

    private final Map<Long, RenderableChunk> chunks = new ConcurrentHashMap<>();
    private final AtomicBoolean initialized = new AtomicBoolean(false);
    private ExecutorService mesherExecutor;
    private StreamingBuffer streamingBuffer;
    private ChunkMesher chunkMesher;
    private World world;

    private void init() {
        this.mesherExecutor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        this.streamingBuffer = new StreamingBuffer(256 * 1024 * 1024);
        this.chunkMesher = new ChunkMesher();
        BariumMod.LOGGER.info("Barium Render Manager inicializado.");
    }

    private void ensureInitialized() {
        if (this.initialized.compareAndSet(false, true)) this.init();
    }

    public boolean isActive() { return this.initialized.get(); }

    public void onWorldChange(@Nullable ClientWorld newWorld) {
        this.ensureInitialized();
        this.world = newWorld;
        if (!this.chunks.isEmpty()) {
            this.chunks.values().forEach(RenderableChunk::delete);
            this.chunks.clear();
        }
    }

    public void scheduleRebuild(int sectionX, int sectionY, int sectionZ, boolean isPriority) {
        this.ensureInitialized();
        if (this.world == null) return;
        BlockPos origin = new BlockPos(sectionX << 4, sectionY << 4, sectionZ << 4);
        RenderableChunk chunk = this.chunks.computeIfAbsent(origin.asLong(), k -> new RenderableChunk(origin));
        if (chunk.needsRebuild()) {
            this.mesherExecutor.submit(new ChunkRebuildTask(chunk, this.world, this.chunkMesher));
        }
    }

    public void uploadMeshedChunk(RenderableChunk chunk) {
        if (!this.isActive()) return;
        ChunkMesher.Result result = chunk.getMeshResult();
        if (result == null || result.isEmpty()) return;
        
        for (Map.Entry<RenderLayer, ByteBuffer> entry : result.layerBuffers().entrySet()) {
            RenderLayer layer = entry.getKey();
            ByteBuffer buffer = entry.getValue();
            
            StreamingBuffer.Region region = this.streamingBuffer.alloc(buffer.remaining());
            this.streamingBuffer.upload(region, buffer);
            chunk.upload(layer, region);
        }
        result.free();
        chunk.setMeshResult(null);
    }
    
    public void render(MatrixStack matrices, Camera camera, Frustum frustum) {
        if (!this.isActive()) return;

        double camX = camera.getPos().getX();
        double camY = camera.getPos().getY();
        double camZ = camera.getPos().getZ();

        matrices.push();
        matrices.translate(-camX, -camY, -camZ);
        
        for (RenderLayer layer : CHUNK_LAYERS) {
            layer.startDrawing();
            this.streamingBuffer.bind();
            BariumVertexFormat.setupAttributes();
            
            for (RenderableChunk chunk : this.chunks.values()) {
                if (frustum != null && !frustum.isVisible(chunk.getBoundingBox())) {
                    continue;
                }
                
                matrices.push();
                matrices.translate(chunk.origin.getX(), chunk.origin.getY(), chunk.origin.getZ());
                
                // CORREÇÃO: Usa a sobrecarga mais simples de setProjectionMatrix.
                // Isso é suficiente para posicionar os chunks corretamente.
                RenderSystem.setProjectionMatrix(matrices.peek().getPositionMatrix());
                
                chunk.draw(layer);
                
                matrices.pop();
            }
            
            BariumVertexFormat.clearAttributes();
            layer.endDrawing();
        }
        
        matrices.pop();
    }

    private static class ChunkRebuildTask implements Runnable {
        private final RenderableChunk chunk;
        private final World world;
        private final ChunkMesher mesher;

        public ChunkRebuildTask(RenderableChunk chunk, World world, ChunkMesher mesher) {
            this.chunk = chunk;
            this.world = world;
            this.mesher = mesher;
        }

        @Override
        public void run() {
            ChunkMesher.Result result = this.mesher.mesh(this.world, chunk.origin);
            MinecraftClient.getInstance().execute(() -> {
                chunk.setMeshResult(result);
                BariumRenderManager.getInstance().uploadMeshedChunk(chunk);
            });
        }
    }
}