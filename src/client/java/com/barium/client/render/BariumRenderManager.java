package com.barium.client.render;

import com.barium.BariumMod;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
// ADICIONE ESTA LINHA:
import net.minecraft.client.world.ClientWorld; 
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BariumRenderManager {
    private static final BariumRenderManager INSTANCE = new BariumRenderManager();
    public static BariumRenderManager getInstance() { return INSTANCE; }

    private final Map<Long, RenderableChunk> chunks = new ConcurrentHashMap<>();
    private ExecutorService mesherExecutor;
    private StreamingBuffer streamingBuffer;
    private ChunkMesher chunkMesher;
    private World world;

    public void init() {
        this.mesherExecutor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        this.streamingBuffer = new StreamingBuffer(256 * 1024 * 1024);
        this.chunkMesher = new ChunkMesher();
        BariumMod.LOGGER.info("Barium Render Manager inicializado.");
    }

    public void onWorldChange(@Nullable ClientWorld newWorld) {
        this.world = newWorld;
        this.chunks.values().forEach(RenderableChunk::delete);
        this.chunks.clear();
    }

    public void scheduleRebuild(int x, int y, int z, boolean isPriority) {
        if (this.world == null) return;
        BlockPos origin = new BlockPos(x, y, z);
        RenderableChunk chunk = this.chunks.computeIfAbsent(origin.asLong(), k -> new RenderableChunk(x, y, z));
        if (chunk.needsRebuild()) {
            this.mesherExecutor.submit(new ChunkRebuildTask(chunk, this.world, this.chunkMesher));
        }
    }

    public void uploadMeshedChunk(RenderableChunk chunk) {
        ChunkMesher.Result result = chunk.getMeshResult();
        if (result == null || result.isEmpty()) return;
        for (Map.Entry<RenderLayer, ByteBuffer> entry : result.layerBuffers().entrySet()) {
            StreamingBuffer.Region region = this.streamingBuffer.alloc(entry.getValue().remaining());
            this.streamingBuffer.upload(region, entry.getValue());
            chunk.upload(entry.getKey(), region);
        }
        if (result != null) {
            result.free();
        }
        chunk.setMeshResult(null);
    }

    public void shutdown() {
        if (this.mesherExecutor != null) this.mesherExecutor.shutdown();
        if (this.streamingBuffer != null) this.streamingBuffer.delete();
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